package com.toocol.ssh.core.mosh.handlers;

import com.toocol.ssh.core.cache.MoshSessionCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.mosh.MoshAddress.MOSH_TICK;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/14 21:17
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class BlockingTickHandler extends BlockingMessageHandler<Void> {

    private final MoshSessionCache moshSessionCache = MoshSessionCache.getInstance();

    public BlockingTickHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return MOSH_TICK;
    }

    @Override
    protected <T> void handleBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        long sessionId = cast(message.body());
        MoshSession moshSession = moshSessionCache.get(sessionId);

        while (moshSession != null && moshSession.isConnected()) {

            moshSession.tick();

            Thread.sleep(1);

        }

        promise.complete();
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
