package com.toocol.ssh.core.term.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:11
 * @version: 0.0.1
 */
public enum TermTheme {
    DARK_THEME("dark", 234, 43, 43, 229, 0, 15),
    LIGHT_THEME("light",231, 36, 36, 31, 15, 0),
    ;
    public final String name;
    public final int executeLineBackgroundColor;
    public final int commandHighlightColor;
    public final int sessionAliveColor;
    public final int hostHighlightColor;
    public final int infoBarFront;
    public final int infoBarBackground;

    public static TermTheme nameOf(String name) {
        for (TermTheme theme : values()) {
            if (theme.name.equals(name)) {
                return theme;
            }
        }
        return null;
    }

    TermTheme(String name, int executeLineBackgroundColor, int commandHighlightColor, int sessionAliveColor, int hostHighlightColor, int infoBarFront, int infoBarBackground) {
        this.name = name;
        this.executeLineBackgroundColor = executeLineBackgroundColor;
        this.commandHighlightColor = commandHighlightColor;
        this.sessionAliveColor = sessionAliveColor;
        this.hostHighlightColor = hostHighlightColor;
        this.infoBarFront = infoBarFront;
        this.infoBarBackground = infoBarBackground;
    }
}
