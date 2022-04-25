package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.toocol.ssh.core.ssh.SshAddress.ACTIVE_SSH_SESSION;

/**
 * Active an ssh session without enter the Shell.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:49
 * @version: 0.0.1
 */
public final class BlockingActiveSessionHandler extends AbstractBlockingMessageHandler<JsonObject> {

    public BlockingActiveSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<JsonObject> promise, Message<T> message) throws Exception {
        int index = cast(message.body());

        //TODO: see com.toocol.ssh.core.ssh.handlers.BlockingEstablishSessionHandler
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<JsonObject> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            message.reply(true);
        } else {
            message.reply(false);
        }
    }

    @Override
    public IAddress consume() {
        return ACTIVE_SSH_SESSION;
    }
}
