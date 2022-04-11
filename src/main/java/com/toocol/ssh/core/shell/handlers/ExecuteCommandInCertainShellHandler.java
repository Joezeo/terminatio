package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.core.CmdFeedbackExtractor;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXHIBIT_SHELL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 22:45
 * @version: 0.0.1
 */
public class ExecuteCommandInCertainShellHandler extends AbstractMessageHandler<String> {

    private final SessionCache sessionCache = SessionCache.getInstance();

    public ExecuteCommandInCertainShellHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");

        Cache.JUST_CLOSE_EXHIBIT_SHELL = true;
        ChannelShell channelShell = sessionCache.getChannelShell(sessionId);
        Shell shell = sessionCache.getShell(sessionId);

        if (channelShell == null || shell == null) {
            promise.fail("ChannelExec or shell is null.");
            return;
        }

        InputStream inputStream = channelShell.getInputStream();
        OutputStream outputStream = shell.getOutputStream();
        outputStream.write((cmd + StrUtil.LF).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        String feedback = new CmdFeedbackExtractor(inputStream, cmd, shell).extractFeedback();

        eventBus.send(EXHIBIT_SHELL.address(), sessionId);

        promise.complete(feedback);
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            message.reply(asyncResult.result());
        } else {
            message.reply(null);
        }
    }
}
