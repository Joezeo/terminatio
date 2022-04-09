package com.toocol.ssh.core.shell.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:39
 * @version: 0.0.1
 */
public class DfHandler extends AbstractMessageHandler<Void> {

    public DfHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return null;
    }

    @Override
    protected <T> void handleWithin(Promise<Void> promise, Message<T> message) throws Exception {

    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }

}