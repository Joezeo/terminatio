package com.toocol.termio.core.term.core;

import com.toocol.termio.core.Termio;
import com.toocol.termio.utilities.action.AbstractDevice;
import com.toocol.termio.utilities.anis.AnisStringBuilder;
import com.toocol.termio.utilities.console.Console;
import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.eventbus.EventBus;
import jline.console.ConsoleReader;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public final class Term extends AbstractDevice {

    public static final String PROMPT = " [termio] > ";
    public static final int TOP_MARGIN = 1;
    public static final int LEFT_MARGIN = 0;
    public static final int TEXT_LEFT_MARGIN = 1;
    static final Console CONSOLE = Console.get();
    private static final Term INSTANCE = new Term();
    public static volatile int WIDTH = CONSOLE.getWindowWidth();
    public static volatile int HEIGHT = CONSOLE.getWindowHeight();
    public static volatile TermStatus status = TermStatus.TERMIO;
    public static TermTheme theme = TermTheme.DARK_THEME;
    public static int executeLine = 0;
    final EscapeHelper escapeHelper;
    final TermHistoryCmdHelper historyCmdHelper;
    final TermReader termReader;
    final TermPrinter termPrinter;
    final TermCharEventDispatcher termCharEventDispatcher;
    ConsoleReader reader;
    volatile StringBuilder lineBuilder = new StringBuilder();
    volatile AtomicInteger executeCursorOldX = new AtomicInteger(0);
    int displayZoneBottom = 0;
    char lastChar = '\0';

    {
        try {
            reader = new ConsoleReader();
        } catch (Exception e) {
            MessageBox.setExitMessage("Create console reader failed.");
            System.exit(-1);
        }
    }

    public Term() {
        escapeHelper = new EscapeHelper();
        historyCmdHelper = new TermHistoryCmdHelper();
        termReader = new TermReader(this);
        termPrinter = new TermPrinter(this);
        termCharEventDispatcher = new TermCharEventDispatcher();
    }

    public static Term getInstance() {
        return INSTANCE;
    }

    public static int getPromptLen() {
        return PROMPT.length() + LEFT_MARGIN;
    }

    public void printScene(boolean resize) {
        termPrinter.printScene(resize);
    }

    public void printTermPrompt() {
        termPrinter.printTermPrompt();
    }

    public void printExecution(String msg) {
        termPrinter.printExecution(msg);
    }

    public void printDisplay(String msg) {
        termPrinter.printDisplay(msg);
    }

    public void printDisplay(String msg,Boolean judgment) {
        termPrinter.printDisplay(msg,judgment);
    }

    public void printErr(String msg) {
        termPrinter.printDisplay(
                new AnisStringBuilder()
                        .front(theme.errorMsgColor.color)
                        .background(theme.displayBackGroundColor.color)
                        .append(msg)
                        .toString()
        );
    }

    public void printDisplayBuffer() {
        termPrinter.printDisplayBuffer();
    }

    public void printCommandBuffer() {
        termPrinter.printCommandBuffer();
    }

    public void printDisplayEcho(String msg) {
        termPrinter.printDisplayEcho(msg);
    }

    public void printExecuteBackground() {
        termPrinter.printExecuteBackground();
    }

    public void printTest() {
        termPrinter.printTest();
    }

    public void printColorPanel() {
        termPrinter.printColorPanel();
    }

    public String readLine() {
        return termReader.readLine();
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int[] getCursorPosition() {
        String[] coord = CONSOLE.getCursorPosition().split(",");
        return new int[]{Integer.parseInt(coord[0]), Integer.parseInt(coord[1])};
    }

    public void setCursorPosition(int x, int y) {
        CONSOLE.setCursorPosition(x, y);
    }

    public void showCursor() {
        CONSOLE.showCursor();
    }

    public void hideCursor() {
        CONSOLE.hideCursor();
    }

    public void cursorLeft() {
        CONSOLE.cursorLeft();
    }

    public void cursorRight() {
        CONSOLE.cursorRight();
    }

    public void cursorBackLine(int lines) {
        CONSOLE.cursorBackLine(lines);
    }

    public EventBus eventBus() {
        return Termio.eventBus();
    }
}