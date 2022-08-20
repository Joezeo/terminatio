package com.toocol.termio.utilities.module;

import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:46
 */
public abstract class BlockingMessageHandler<R> extends AbstractMessageHandler {
    /**
     * whether the handler is handle parallel
     */
    private final boolean parallel;

    public BlockingMessageHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context);
        this.parallel = parallel;
    }

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>     generic type
     */
    public <T> void handle(Message<T> message) {
        context.executeBlocking(
                promise -> {
                    try {
                        handleBlocking(cast(promise), message);
                    } catch (Exception e) {
                        MessageBox.setExitMessage("Caught exception, exit program, message = " + e.getMessage());
                        error("Caught exception, exit program, stackTrace : {}", parseStackTrace(e));
                        System.exit(-1);
                    }
                },
                !parallel,
                asyncResult -> {
                    try {
                        resultBlocking(cast(asyncResult), message);
                    } catch (Exception e) {
                        MessageBox.setExitMessage("Caught exception, exit program, message = " + e.getMessage());
                        error("Caught exception, exit program, stackTrace : {}", parseStackTrace(e));
                        System.exit(-1);
                    }
                }
        );
    }

    /**
     * execute the blocked process
     *
     * @param promise promise
     * @param message message
     * @param <T>     generic type
     * @throws Exception exception
     */
    protected abstract <T> void handleBlocking(Promise<R> promise, Message<T> message) throws Exception;

    /**
     * response the blocked process result
     *
     * @param asyncResult async result
     * @param message     message
     * @param <T>         generic type
     * @throws Exception exception
     */
    protected abstract <T> void resultBlocking(AsyncResult<R> asyncResult, Message<T> message) throws Exception;
}