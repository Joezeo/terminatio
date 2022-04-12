package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.CmdUtil;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.StrUtil;
import jline.ConsoleReader;
import lombok.AllArgsConstructor;
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

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 20:57
 */
public class Shell {
    public static final Pattern PROMPT_PATTERN = Pattern.compile("(\\[(.*?)]#)");

    private static ConsoleReader reader = null;

    static {
        try {
            reader = new ConsoleReader(System.in, null);
        } catch (IOException e) {
            Printer.println("Register console reader failed.");
            System.exit(-1);
        }
    }

    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private final OutputStream outputStream;
    private InputStream inputStream;

    private volatile boolean returnWrite = false;
    private volatile boolean promptNow = false;

    public volatile AtomicReference<String> localLastCmd = new AtomicReference<>(StrUtil.EMPTY);
    public volatile AtomicReference<String> remoteCmd = new AtomicReference<>(StrUtil.EMPTY);
    protected volatile AtomicReference<String> currentPrint = new AtomicReference<>(StrUtil.EMPTY);
    protected volatile AtomicReference<String> selectHistoryCmd = new AtomicReference<>(StrUtil.EMPTY);
    protected volatile String localLastInput = StrUtil.EMPTY;
    protected volatile String lastRemoteCmd = StrUtil.EMPTY;
    private volatile Status status = Status.NORMAL;

    private final ShellPrinter shellPrinter = new ShellPrinter(this);
    protected final Set<String> tabFeedbackRec = new HashSet<>();

    private final StringBuffer cmd = new StringBuffer();
    private final AtomicReference<String> welcome = new AtomicReference<>();
    private final AtomicReference<String> prompt = new AtomicReference<>();
    private final AtomicReference<String> user = new AtomicReference<>();
    private final AtomicReference<String> fullPath = new AtomicReference<>();

    @AllArgsConstructor
    public enum Status {
        /**
         * The status of Shell.
         */
        NORMAL(1, "Shell is under normal cmd input status."),
        TAB_ACCOMPLISH(2, "Shell is under tab key to auto-accomplish address status."),
        VIM_BEFORE(3, "Shell is before Vim/Vi edit status."),
        VIM_UNDER(4, "Shell is under Vim/Vi edit status."),
        HISTORY_COMMAND_SELECT(5, "Shell is under history command select status."),
        ;

        public final int status;
        public final String comment;
    }

    public Shell(OutputStream outputStream, InputStream inputStream) {
        assert outputStream != null && inputStream != null;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.initialFirstCorrespondence();
    }

    public void print(String msg) {
        Matcher matcher = PROMPT_PATTERN.matcher(msg);
        if (matcher.find()) {
            prompt.set(matcher.group(0) + StrUtil.SPACE);
            extractUserFromPrompt();
            if (status.equals(Status.VIM_UNDER)) {
                status = Status.NORMAL;
                Printer.clear();
            }
        }

        switch (status) {
            case NORMAL -> shellPrinter.printInNormal(msg);
            case TAB_ACCOMPLISH -> shellPrinter.printInTabAccomplish(msg);
            case VIM_BEFORE, VIM_UNDER -> shellPrinter.printInVim(msg);
            case HISTORY_COMMAND_SELECT -> shellPrinter.printSelectHistoryCommand(msg);
            default -> {
            }
        }
    }

    public String readCmd() throws Exception {
        cmd.delete(0, cmd.length());
        StringBuilder localLastInputBuffer = new StringBuilder();
        while (true) {
            char inChar = (char) reader.readVirtualKey();
            if (status.equals(Status.VIM_UNDER)) {
                outputStream.write(inChar);
                outputStream.flush();
            } else {
                if (inChar == CharUtil.CTRL_C) {

                    outputStream.write(inChar);
                    outputStream.flush();

                } else if (inChar == CharUtil.UP_ARROW || inChar == CharUtil.DOWN_ARROW) {

                    status = Status.HISTORY_COMMAND_SELECT;
                    localLastCmd.set("");
                    outputStream.write(inChar);
                    outputStream.flush();

                } else if (inChar == CharUtil.TAB) {

                    if (status.equals(Status.NORMAL)) {
                        localLastCmd.set(cmd.toString());
                        remoteCmd.set(cmd.toString());
                    }
                    localLastInput = localLastInputBuffer.toString();
                    localLastInputBuffer = new StringBuilder();
                    cmd.append(inChar);
                    tabFeedbackRec.clear();
                    outputStream.write(cmd.append(CharUtil.TAB).toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    cmd.delete(0, cmd.length());
                    status = Status.TAB_ACCOMPLISH;

                } else if (inChar == CharUtil.BACKSPACE) {

                    if (cmd.toString().trim().length() == 0 && status.equals(Status.NORMAL)) {
                        continue;
                    }
                    if (remoteCmd.get().length() == 0 && status.equals(Status.TAB_ACCOMPLISH)) {
                        continue;
                    }
                    if (selectHistoryCmd.get().length() == 0 && status.equals(Status.HISTORY_COMMAND_SELECT)) {
                        continue;
                    }

                    if (status.equals(Status.TAB_ACCOMPLISH)) {
                        // This is ctrl+backspace
                        cmd.append('\u007F');
                        if (remoteCmd.get().length() > 0) {
                            remoteCmd.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
                        }
                        if (localLastCmd.get().length() > 0) {
                            localLastCmd.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
                        }
                    }
                    if (status.equals(Status.NORMAL)) {
                        cmd.deleteCharAt(cmd.length() - 1);
                    }
                    if (status.equals(Status.HISTORY_COMMAND_SELECT)) {
                        cmd.append('\u007F');
                        selectHistoryCmd.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
                    }

                    if (localLastInputBuffer.length() > 0) {
                        localLastInputBuffer = new StringBuilder(localLastInputBuffer.substring(0, localLastInputBuffer.length() - 1));
                    }
                    Printer.virtualBackspace();

                } else if (inChar == CharUtil.CR || inChar == CharUtil.LF) {

                    if (status.equals(Status.TAB_ACCOMPLISH)) {
                        localLastCmd.set(remoteCmd.get() + StrUtil.CRLF);
                    }
                    localLastInput = localLastInputBuffer.toString();
                    currentPrint.set(StrUtil.EMPTY);
                    lastRemoteCmd = remoteCmd.get();
                    remoteCmd.set(StrUtil.EMPTY);
                    selectHistoryCmd.set(StrUtil.EMPTY);
                    Printer.print(StrUtil.CRLF);
                    status = Status.NORMAL;

                    break;
                } else if (CharUtil.isAsciiPrintable(inChar)){

                    if (status.equals(Status.TAB_ACCOMPLISH)) {
                        remoteCmd.getAndUpdate(prev -> prev + inChar);
                        localLastCmd.getAndUpdate(prev -> prev + inChar);
                    }
                    currentPrint.getAndUpdate(prev -> prev + inChar);
                    cmd.append(inChar);
                    localLastInputBuffer.append(inChar);
                    Printer.print(String.valueOf(inChar));

                }
            }
        }

        String cmdStr = cmd.toString();
        boolean isVimCmd = (StringUtils.isEmpty(cmd) && CmdUtil.isViVimCmd(localLastCmd.get())) || CmdUtil.isViVimCmd(cmd.toString());
        if (isVimCmd) {
            status = Status.VIM_BEFORE;
        }

        return cmdStr;
    }

    public void printErr(String err) {
        shellPrinter.printErr(err);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWelcome() {
        return StringUtils.isEmpty(welcome.get()) ? null : welcome.get();
    }

    public String getPrompt() {
        return prompt.get();
    }

    public AtomicReference<String> getUser() {
        return user;
    }

    public AtomicReference<String> getFullPath() {
        return fullPath;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getLastRemoteCmd() {
        return lastRemoteCmd;
    }

    public void setPrompt(String prompt) {
        this.prompt.set(prompt);
    }

    public void setFullPath(String fullPath) {
        this.fullPath.set(fullPath);
    }

    @SuppressWarnings("all")
    private void initialFirstCorrespondence() {
        try {
            CountDownLatch mainLatch = new CountDownLatch(2);

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
                    while (true) {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);

                            Matcher matcher = PROMPT_PATTERN.matcher(inputStr);
                            if (matcher.find()) {
                                prompt.set(matcher.group(0) + StrUtil.SPACE);
                                returnWrite = true;
                                break;
                            } else {
                                welcome.set(inputStr);
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
        fullPath.set("/" + user.get());
        // this InputStream is already invalid.
        this.inputStream = null;
    }

    public void extractUserFromPrompt() {
        String preprocess = prompt.get().trim().replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("#", "")
                .trim();
        user.set(preprocess.split("@")[0]);
    }

}
