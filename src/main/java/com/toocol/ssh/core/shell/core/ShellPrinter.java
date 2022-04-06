package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.Printer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
public class ShellPrinter {

    private final Shell shell;

    public ShellPrinter(Shell shell) {
        this.shell = shell;
    }

    protected void printInNormal(String msg) {
        if (shell.localLastCmd.get().equals(msg)) {
            return;
        } else if (msg.startsWith("\b\u001B[K")) {
            String[] split = msg.split("\r\n");
            if (split.length == 1) {
                return;
            }
            msg = split[1];
        } else if (msg.startsWith(shell.localLastCmd.get().trim())) {
            // cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(shell.localLastCmd.get().trim().length());
        }
        if (msg.startsWith("\r\n")) {
            msg = msg.replaceFirst("\r\n", "");
        }

        String tmp = msg;
        shell.currentPrint.getAndUpdate(prev -> prev + tmp);
        Printer.print(msg);
    }

    protected void printInTabAccomplish(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }

        if (msg.contains("\u001B")) {
            return;
        }

        msg = msg.replaceAll("\b", "");

        if (msg.trim().equals(shell.localLastCmd.get().replaceAll("\t", ""))) {
            if (msg.endsWith(" ")) {
                Printer.print(" ");
            }
            return;
        }

        if (msg.equals(shell.localLastInput)) {
            return;
        }

        // remove system prompt voice
        if (msg.contains("\u0007")) {
            msg = msg.replaceAll("\u0007", "");
            Printer.print(msg);
            String tmp = msg;
            shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
            shell.localLastCmd.getAndUpdate(prev -> prev.replaceAll("\t", "") + tmp);
            return;
        }

        if (StringUtils.isEmpty(msg)) {
            return;
        }

        if (!msg.contains("\r\n")) {
            Printer.print(msg);
            String tmp = msg;
            shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
            shell.localLastCmd.getAndUpdate(prev -> prev.replaceAll("\t", "") + tmp);
            return;
        }
        String[] split = msg.split("\r\n");
        if (split.length != 0) {
            String localLine = shell.getPrompt()
                    + shell.localLastCmd.get()
                    .replaceAll("\t", "")
                    .replaceAll("\b", "")
                    .replaceAll("\u001B", "")
                    .replaceAll("\\[k", "")
                    .replaceAll("\\[K", "");
            if (!split[split.length - 1].equals(localLine)) {
                // have already auto-accomplish address
                int backspaceLen = (localLine + shell.currentPrint).length();
                for (int idx = 0; idx < backspaceLen; idx++) {
                    Printer.print("\b");
                }
                msg = split[split.length - 1];
                if (msg.split("#").length == 2) {
                    shell.remoteCmd.set(msg.split("#")[1].trim());
                }
            } else {
                for (String input : split) {
                    if (StringUtils.isEmpty(input)) {
                        continue;
                    }
                    if (input.split("#").length == 2) {
                        shell.remoteCmd.set(msg.split("#")[1].trim());
                    }
                    if (shell.tabFeedbackRec.contains(input)) {
                        continue;
                    }
                    Printer.print("\r\n" + input);
                    shell.currentPrint.set(input);
                    shell.tabFeedbackRec.add(input);
                }
                return;
            }
        }
        if (msg.startsWith("\r\n")) {
            msg = msg.replaceFirst("\r\n", "");
        }

        String tmp = msg;
        shell.currentPrint.getAndUpdate(prev -> prev + tmp);
        Printer.print(msg);
    }
}