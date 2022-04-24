package com.toocol.ssh.core.file.core;

import com.toocol.ssh.utilities.console.Console;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 22:07
 * @version: 0.0.1
 */
public class DirectoryChooser {

    private static final Console CONSOLE = Console.get();

    /**
     * chose the local path
     *
     * @return file paths
     */
    public String showOpenDialog() {
        return CONSOLE.chooseDirectory();
    }

}
