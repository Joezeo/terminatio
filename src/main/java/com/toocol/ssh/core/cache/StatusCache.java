package com.toocol.ssh.core.cache;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/1 0:08
 * @version: 0.0.1
 */
public class StatusCache {

    public volatile static boolean LOADING_ACCOMPLISH = false;

    public volatile static boolean STOP_ACCEPT_OUT_COMMAND = false;

    public volatile static boolean HANGED_QUIT = false;

    public volatile static boolean HANGED_ENTER = false;

    public volatile static boolean JUST_CLOSE_EXHIBIT_SHELL = false;

    public volatile static boolean SHOW_WELCOME = false;

    public volatile static boolean ACCEPT_SHELL_CMD_IS_RUNNING = false;

    public volatile static boolean ACCESS_EXHIBIT_SHELL_WITH_PROMPT = true;

    public volatile static boolean STOP_LISTEN_TERMINAL_SIZE_CHANGE = false;

}
