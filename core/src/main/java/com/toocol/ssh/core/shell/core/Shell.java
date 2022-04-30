package com.toocol.ssh.core.shell.core;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.core.shell.handlers.BlockingDfHandler;
import com.toocol.ssh.core.term.core.EscapeHelper;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.action.AbstractDevice;
import com.toocol.ssh.utilities.execeptions.RemoteDisconnectException;
import com.toocol.ssh.utilities.utils.CmdUtil;
import com.toocol.ssh.utilities.utils.StrUtil;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toocol.ssh.core.shell.ShellAddress.START_DF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 20:57
 */
public final class Shell extends AbstractDevice {

    static final Pattern PROMPT_PATTERN = Pattern.compile("(\\[(\\w*?)@(.*?)][$#])");

    private ConsoleReader reader;
    {
        try {
            reader = new ConsoleReader(System.in, null, null);
        } catch (Exception e) {
            Printer.println("\nCreate console reader failed.");
            System.exit(-1);
        }
    }

    /**
     * the session's id that shell belongs to.
     */
    private final long sessionId;
    /**
     * the EventBus of vert.x system.
     */
    private final EventBus eventBus;
    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private OutputStream outputStream;
    private InputStream inputStream;
    /**
     * JSch channel shell
     */
    private ChannelShell channelShell;
    /**
     * Mosh session
     */
    private MoshSession moshSession;

    private volatile boolean returnWrite = false;
    private volatile boolean promptNow = false;
    private volatile boolean initOnce = false;

    final Term term = Term.getInstance();
    final ShellPrinter shellPrinter;
    final ShellReader shellReader;
    final HistoryCmdHelper historyCmdHelper;
    final MoreHelper moreHelper;
    final EscapeHelper escapeHelper;
    final VimHelper vimHelper;
    final ShellCharEventDispatcher shellCharEventDispatcher;

    volatile StringBuffer localLastCmd = new StringBuffer();
    volatile StringBuffer remoteCmd = new StringBuffer();
    volatile StringBuffer currentPrint = new StringBuffer();
    volatile StringBuffer selectHistoryCmd = new StringBuffer();
    volatile StringBuffer localLastInput = new StringBuffer();
    volatile StringBuffer lastRemoteCmd = new StringBuffer();
    volatile StringBuffer lastExecuteCmd = new StringBuffer();
    volatile Status status = Status.NORMAL;

    final Set<String> tabFeedbackRec = new HashSet<>();

    final StringBuilder cmd = new StringBuilder();
    volatile AtomicReference<String> prompt = new AtomicReference<>();
    volatile AtomicReference<String> fullPath = new AtomicReference<>();
    volatile String welcome = null;
    volatile String user = null;
    volatile String bottomLinePrint = StrUtil.EMPTY;

    public enum Status {
        /**
         * The status of Shell.
         */
        NORMAL(1, "Shell is under normal cmd input status."),
        TAB_ACCOMPLISH(2, "Shell is under tab key to auto-accomplish address status."),
        VIM_BEFORE(3, "Shell is before Vim/Vi edit status."),
        VIM_UNDER(4, "Shell is under Vim/Vi edit status."),
        MORE_BEFORE(5, "Shell is before more cmd process status."),
        MORE_PROC(6, "Shell is under more cmd process status."),
        MORE_EDIT(7, "Shell is under more regular expression or cmd edit status."),
        MORE_SUB(8, "Shell is under more :sub cmd status."),
        ;

        Status(int status, String comment) {
            this.status = status;
            this.comment = comment;
        }

        public final int status;
        public final String comment;
    }

    public void resetIO(ShellProtocol protocol) {
        try {
            switch (protocol) {
                case SSH -> {
                    this.inputStream = channelShell.getInputStream();
                    this.outputStream = channelShell.getOutputStream();
                }
                case MOSH -> {
                    this.inputStream = moshSession.getInputStream();
                    this.outputStream = moshSession.getOutputStream();
                }
            }
        } catch (Exception e) {
            Printer.printErr("Reset IO failed: " + e.getMessage());
            System.exit(-1);
        }
    }

    public Shell(long sessionId, EventBus eventBus, MoshSession moshSession) {
        this.sessionId = sessionId;
        this.eventBus = eventBus;
        this.moshSession = moshSession;
        this.shellPrinter = new ShellPrinter(this);
        this.shellReader = new ShellReader(this, reader);
        this.historyCmdHelper = new HistoryCmdHelper(this);
        this.moreHelper = new MoreHelper();
        this.escapeHelper = new EscapeHelper();
        this.vimHelper = new VimHelper();
        this.shellCharEventDispatcher = new ShellCharEventDispatcher();

        this.shellReader.initReader();
    }

    public Shell(long sessionId, EventBus eventBus, ChannelShell channelShell) {
        this.sessionId = sessionId;
        this.eventBus = eventBus;
        this.channelShell = channelShell;
        this.shellPrinter = new ShellPrinter(this);
        this.shellReader = new ShellReader(this, reader);
        this.historyCmdHelper = new HistoryCmdHelper(this);
        this.moreHelper = new MoreHelper();
        this.escapeHelper = new EscapeHelper();
        this.vimHelper = new VimHelper();
        this.shellCharEventDispatcher = new ShellCharEventDispatcher();

        this.shellReader.initReader();
    }

    public boolean print(String msg) {
        Matcher matcher = PROMPT_PATTERN.matcher(msg.trim());
        if (matcher.find()) {
            prompt.set(matcher.group(0) + StrUtil.SPACE);
            extractUserFromPrompt();
            if (status.equals(Status.VIM_UNDER)) {
                status = Status.NORMAL;
            } else if (status.equals(Status.MORE_PROC) || status.equals(Status.MORE_EDIT) || status.equals(Status.MORE_SUB)) {
                status = Status.NORMAL;
            }
        }

        if (status.equals(Status.MORE_BEFORE)) {
            status = Status.MORE_PROC;
        }

        boolean hasPrint = false;
        switch (status) {
            case NORMAL -> hasPrint = shellPrinter.printInNormal(msg);
            case TAB_ACCOMPLISH -> shellPrinter.printInTabAccomplish(msg);
            case VIM_BEFORE, VIM_UNDER -> shellPrinter.printInVim(msg);
            case MORE_BEFORE, MORE_PROC, MORE_EDIT, MORE_SUB -> shellPrinter.printInMore(msg);
            default -> {
            }
        }

        if (status.equals(Shell.Status.VIM_BEFORE)) {
            status = Shell.Status.VIM_UNDER;
        }

        selectHistoryCmd.delete(0, selectHistoryCmd.length());
        localLastCmd.delete(0, localLastCmd.length());
        return hasPrint;
    }

    public String readCmd() throws Exception {
        try {
            shellReader.readCmd();
        } catch (RuntimeException e) {
            return null;
        }

        String cmdStr = cmd.toString();
        boolean isVimCmd = CmdUtil.isViVimCmd(localLastCmd.toString())
                || CmdUtil.isViVimCmd(cmdStr)
                || CmdUtil.isViVimCmd(selectHistoryCmd.toString());
        if (isVimCmd) {
            status = Status.VIM_BEFORE;
        }

        if (CmdUtil.isCdCmd(lastRemoteCmd.toString())
                || CmdUtil.isCdCmd(cmdStr)
                || CmdUtil.isCdCmd(selectHistoryCmd.toString())) {
            StatusCache.EXECUTE_CD_CMD = true;
        }

        boolean isMoreCmd = (CmdUtil.isMoreCmd(localLastCmd.toString()) && !"more".equals(localLastCmd.toString().trim()))
                || (CmdUtil.isMoreCmd(cmdStr) && !"more".equals(cmdStr.trim()))
                || (CmdUtil.isMoreCmd(selectHistoryCmd.toString()) && !"more".equals(selectHistoryCmd.toString().trim()));
        if (isMoreCmd) {
            status = Shell.Status.MORE_BEFORE;
        }

        lastRemoteCmd.delete(0, lastRemoteCmd.length());
        currentPrint.delete(0, currentPrint.length());
        return cmdStr;
    }

    public void extractUserFromPrompt() {
        String preprocess = prompt.get().trim().replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("#", "")
                .trim();
        user = preprocess.split("@")[0];
    }

    @SuppressWarnings("all")
    public void initialFirstCorrespondence(ShellProtocol protocol) {
        if (initOnce) {
            return;
        }
        try {
            CountDownLatch mainLatch = new CountDownLatch(2);

            JsonObject request = new JsonObject();
            request.put("sessionId", sessionId);
            request.put("remotePath", "/" + user + "/.bash_history");
            request.put("type", BlockingDfHandler.DF_TYPE_BYTE);
            if (eventBus != null) {
                eventBus.request(START_DF_COMMAND.address(), request, result -> {
                    if (result == null || result.result() == null) {
                        return;
                    }
                    byte[] bytes = (byte[]) result.result().body();
                    String data = new String(bytes, StandardCharsets.UTF_8);
                    historyCmdHelper.initialize(data.split(StrUtil.LF));
                });
            }

            new Thread(() -> {
                try {
                    do {
                        if (returnWrite) {
                            return;
                        }
                    } while (!promptNow);

                    outputStream.write(StrUtil.LF.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mainLatch.countDown();
                }
            }).start();

            new Thread(() -> {
                try {
                    byte[] tmp = new byte[1024];
                    long startTime = System.currentTimeMillis();
                    if (channelShell == null) {
                        promptNow = true;
                    }
                    while (true) {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);

                            Matcher matcher = PROMPT_PATTERN.matcher(inputStr);
                            if (matcher.find()) {
                                prompt.set(matcher.group(0).replaceAll("\\[\\?1034h", "") + StrUtil.SPACE);
                                returnWrite = true;
                                break;
                            } else {
                                welcome = inputStr;
                                returnWrite = true;
                                break;
                            }
                        }

                        if (System.currentTimeMillis() - startTime >= 1000) {
                            promptNow = true;
                        }

                        if (StringUtils.isNoneEmpty(prompt.get())) {
                            mainLatch.countDown();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            mainLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert prompt.get() != null;
        extractUserFromPrompt();
        fullPath.set("/" + user);

        resetIO(protocol);
        this.initOnce = true;
    }

    public void flush() {
        if (SshSessionCache.getInstance().isDisconnect(sessionId)) {
            throw new RemoteDisconnectException("Session disconnect.");
        }
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void write(byte[] bytes) {
        if (SshSessionCache.getInstance().isDisconnect(sessionId)) {
            throw new RemoteDisconnectException("Session disconnect.");
        }
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void write(char bytes) {
        if (SshSessionCache.getInstance().isDisconnect(sessionId)) {
            throw new RemoteDisconnectException("Session disconnect.");
        }
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void writeAndFlush(byte[] bytes) {
        if (SshSessionCache.getInstance().isDisconnect(sessionId)) {
            throw new RemoteDisconnectException("Session disconnect.");
        }
        try {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void writeAndFlush(char inChar) {
        if (SshSessionCache.getInstance().isDisconnect(sessionId)) {
            throw new RemoteDisconnectException("Session disconnect.");
        }
        try {
            outputStream.write(inChar);
            outputStream.flush();
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void clearShellLineWithPrompt() {
        int promptLen = prompt.get().length();
        Tuple2<Integer, Integer> position = term.getCursorPosition();
        int cursorX = position._1();
        int cursorY = position._2();
        term.hideCursor();
        term.setCursorPosition(promptLen, cursorY);
        Printer.print(" ".repeat(cursorX - promptLen));
        term.setCursorPosition(promptLen, cursorY);
        term.showCursor();
    }

    public void cleanUp() {
        remoteCmd.delete(0, remoteCmd.length());
        currentPrint.delete(0, currentPrint.length());
        selectHistoryCmd.delete(0, selectHistoryCmd.length());
        localLastCmd.delete(0, localLastCmd.length());
    }

    public void printErr(String err) {
        shellPrinter.printErr(err);
    }

    public boolean isClosed() {
        if (channelShell != null) {
            return channelShell.isClosed();
        }
        if (moshSession != null) {
            return !moshSession.isConnected();
        }
        return true;
    }

    public void clearRemoteCmd() {
        remoteCmd.delete(0, remoteCmd.length());
    }

    public void setLocalLastCmd(String cmd) {
        localLastCmd.delete(0, localLastCmd.length()).append(cmd);
    }

    public Status getStatus() {
        return status;
    }

    public String getWelcome() {
        return StringUtils.isEmpty(welcome) ? null : welcome;
    }

    public String getPrompt() {
        return prompt.get();
    }

    public String getLastRemoteCmd() {
        return lastRemoteCmd.toString();
    }

    public String getRemoteCmd() {
        return remoteCmd.toString();
    }

    public String getCurrentPrint() {
        return currentPrint.toString();
    }

    public String getUser() {
        return user;
    }

    public AtomicReference<String> getFullPath() {
        return fullPath;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPrompt(String prompt) {
        this.prompt.set(prompt);
    }

    public void setFullPath(String fullPath) {
        this.fullPath.set(fullPath);
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}