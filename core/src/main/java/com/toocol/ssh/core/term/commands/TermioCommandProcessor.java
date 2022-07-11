package com.toocol.ssh.core.term.commands;

import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:32
 */
public abstract class TermioCommandProcessor implements Loggable {

    /**
     * process command
     *
     * @param eventBus event bus
     * @param cmd      cmd
     * @param resultAndMsg    resultAndMsg
     */
    public abstract void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg);

}
