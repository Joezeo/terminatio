package com.toocol.ssh.core.shell.handlers;

import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.CmdFeedbackHelper;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.utilities.sync.SharedCountdownLatch;
import com.toocol.ssh.utilities.utils.StrUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 22:45
 * @version: 0.0.1
 */
public final class BlockingExecuteCmdInShellHandler extends BlockingMessageHandler<String> {

    private final ShellCache shellCache = ShellCache.getInstance();

    public BlockingExecuteCmdInShellHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
    }

    @Override
    protected <T> void handleBlocking(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");
        String prefix = request.getString("prefix");

        SharedCountdownLatch.await(
                () -> {
                    StatusCache.JUST_CLOSE_EXHIBIT_SHELL = true;
                    StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false;
                },
                this.getClass(),
                BlockingShellDisplayHandler.class
        );

        Shell shell = shellCache.getShell(sessionId);

        InputStream inputStream = shell.getInputStream();
        shell.writeAndFlush((cmd + StrUtil.LF).getBytes(StandardCharsets.UTF_8));

        String feedback = new CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback();

        StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT = false;
        eventBus.send(DISPLAY_SHELL.address(), sessionId);

        promise.complete(feedback);
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            message.reply(asyncResult.result());
        } else {
            message.reply(null);
        }
    }
}
