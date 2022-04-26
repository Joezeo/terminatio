package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.ColorHelper;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class FlushCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Printer.printScene(false);
        Term.getInstance().setCursorPosition(4, Term.executeLine);
        Printer.print(ColorHelper.background(Term.PROMPT + " ".repeat(Term.getInstance().getWidth() - Term.getPromptLen() - 4), Term.theme.executeBackgroundColor));
        Term.getInstance().printBackground();

        resultAndMsg.first(true);
    }
}
