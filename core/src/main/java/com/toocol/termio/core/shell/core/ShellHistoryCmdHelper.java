package com.toocol.termio.core.shell.core;

import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.utils.StrUtil;

import java.util.Stack;
import java.util.UUID;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 0:26
 * @version: 0.0.1
 */
public final class ShellHistoryCmdHelper {

    private final Shell shell;
    /**
     * The stack storage all the executed cmd.
     */
    private final Stack<String> baseCmdStack = new Stack<>();
    /**
     * when user press the up arrow, use this stack.
     * clear out when user execute new cmd, which means invoke the method {HistoryCmdHelper.push(String cmd)}
     */
    private final Stack<String> upArrowStack = new Stack<>();
    /**
     * when user press the down arrow, use this stack.
     * clear out when user execute new cmd, which means invoke the method {HistoryCmdHelper.push(String cmd)}
     */
    private final Stack<String> downArrowStack = new Stack<>();
    /**
     * the flag to record user input command;
     */
    private final String flag = UUID.randomUUID().toString();

    private String upBuffer = null;
    private String downBuffer = null;
    private boolean puToDown = false;
    private boolean start = false;

    public ShellHistoryCmdHelper(Shell shell) {
        this.shell = shell;
    }

    public synchronized void initialize(String[] historyCmds) {
        for (String historyCmd : historyCmds) {
            if ("export HISTCONTROL=ignoreboth".equals(historyCmd)) {
                continue;
            }
            baseCmdStack.push(historyCmd);
        }
    }

    public synchronized void push(String cmd) {
        if (baseCmdStack.isEmpty()) {
            baseCmdStack.push(cmd.trim());
        } else if (!baseCmdStack.peek().equals(cmd.trim())) {
            baseCmdStack.push(cmd.trim());
        }
        reset();
    }

    /*
     * when user press the up arrow.
     **/
    public synchronized void up() {
        start = true;
        if (downBuffer != null) {
            downArrowStack.push(downBuffer);
            downBuffer = null;
        }
        if ((upArrowStack.isEmpty() && downArrowStack.isEmpty())
                || (!downArrowStack.isEmpty() && puToDown)) {
            baseCmdStack.forEach(upArrowStack::push);
            puToDown = false;
        }
        String cmd = upArrowStack.pop();
        if (upArrowStack.isEmpty()) {
            upArrowStack.push(cmd);
            if (upBuffer != null) {
                downArrowStack.push(upBuffer);
            }
            upBuffer = null;

            shell.currentPrint.delete(0, shell.currentPrint.length()).append(cmd);
            shell.selectHistoryCmd.delete(0, shell.selectHistoryCmd.length()).append(cmd);
            shell.cmd.delete(0, shell.cmd.length()).append(cmd);

            shell.clearShellLineWithPrompt();
            Printer.print(cmd);
            return;
        }

        if (upBuffer != null) {
            downArrowStack.push(upBuffer);
        }
        shell.currentPrint.delete(0, shell.currentPrint.length()).append(cmd);
        shell.selectHistoryCmd.delete(0, shell.selectHistoryCmd.length()).append(cmd);
        shell.cmd.delete(0, shell.cmd.length()).append(cmd);
        if (StrUtil.EMPTY.equals(cmd)) {
            upBuffer = null;
        } else {
            upBuffer = cmd;
        }

        shell.clearShellLineWithPrompt();
        Printer.print(cmd);
    }

    /*
     * when user press the down arrow
     **/
    public synchronized void down() {
        if (upArrowStack.isEmpty()) {
            return;
        }
        if (upBuffer != null) {
            upArrowStack.push(upBuffer);
            upBuffer = null;
        }
        String cmd;
        if (downArrowStack.isEmpty()) {
            start = false;
            cmd = StrUtil.EMPTY;
        } else {
            cmd = downArrowStack.pop();
        }
        if (downBuffer != null) {
            upArrowStack.push(downBuffer);
        }
        boolean resetFlag = false;
        if (cmd.contains("--" + flag)) {
            cmd = cmd.replaceAll("--" + flag, "");
            resetFlag = true;
        }
        String tmp = cmd;
        shell.currentPrint.delete(0, shell.currentPrint.length()).append(tmp);
        shell.selectHistoryCmd.delete(0, shell.selectHistoryCmd.length()).append(tmp);
        shell.cmd.delete(0, shell.cmd.length()).append(cmd);
        if (StrUtil.EMPTY.equals(cmd)) {
            downBuffer = null;
        } else {
            downBuffer = cmd;
        }
        if (resetFlag) {
            reset();
        }

        shell.clearShellLineWithPrompt();
        Printer.print(cmd);
    }

    public synchronized void pushToDown(String cmd) {
        puToDown = true;
        downArrowStack.push(cmd + "--" + flag);
    }

    public synchronized void reset() {
        upArrowStack.clear();
        downArrowStack.clear();
        upBuffer = null;
        downBuffer = null;
        start = false;
        puToDown = false;
    }

    public boolean isStart() {
        return start;
    }

}