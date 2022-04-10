package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelExec;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.core.ExecChannelProvider;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.util.regex.Matcher;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXECUTE_SINGLE_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:57
 * @version: 0.0.1
 */
public class ExecuteSingleCommandHandler extends AbstractMessageHandler<String> {

    private final SessionCache sessionCache = SessionCache.getInstance();
    private final ExecChannelProvider execChannelProvider = ExecChannelProvider.getInstance();

    public ExecuteSingleCommandHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_SINGLE_COMMAND;
    }

    @Override
    protected <T> void handleWithin(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");

        ChannelExec channelExec = execChannelProvider.getChannelExec(sessionId);
        Shell shell = sessionCache.getShell(sessionId);

        if (channelExec == null || shell == null) {
            promise.fail("ChannelExec or shell is null.");
            return;
        }
        InputStream inputStream = channelExec.getInputStream();

        channelExec.setCommand(cmd);
        channelExec.connect();

        String feedback = null;

        long startTime = System.currentTimeMillis();
        byte[] tmp = new byte[1024];
        do {
            if (System.currentTimeMillis() - startTime >= 10000) {
                feedback = "";
            }
            while (inputStream.available() > 0) {
                int i = inputStream.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                String msg = new String(tmp, 0, i);
                Matcher matcher = Shell.PROMPT_PATTERN.matcher(msg);
                if (!matcher.find()) {
                    feedback = msg;
                }
            }
        } while (feedback == null);

        channelExec.disconnect();
        promise.complete(feedback);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            message.reply(asyncResult.result());
        } else {
            message.reply(null);
        }
    }
}
