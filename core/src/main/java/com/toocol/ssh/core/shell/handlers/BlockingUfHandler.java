package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelSftp;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.utilities.utils.FileNameUtil;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.SftpChannelProvider;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.FileInputStream;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static com.toocol.ssh.core.file.FileAddress.CHOOSE_FILE;
import static com.toocol.ssh.core.shell.ShellAddress.START_UF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:38
 * @version: 0.0.1
 */
public final class BlockingUfHandler extends AbstractBlockingMessageHandler<Void> {

    private final SftpChannelProvider sftpChannelProvider = SftpChannelProvider.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();

    public BlockingUfHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return START_UF_COMMAND;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String remotePath = request.getString("remotePath");

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder localPathBuilder = new StringBuilder();
        eventBus.request(CHOOSE_FILE.address(), null, result -> {
            localPathBuilder.append(Objects.requireNonNullElse(result.result().body(), "-1"));
            latch.countDown();
        });

        ChannelSftp channelSftp = sftpChannelProvider.getChannelSftp(sessionId);
        if (channelSftp == null) {
            shellCache.getShell(sessionId).printErr("Create sftp channel failed.");
            promise.complete();
            return;
        }

        latch.await();

        Shell shell = shellCache.getShell(sessionId);
        Printer.print(shell.getPrompt());

        String fileNames = localPathBuilder.toString();
        if ("-1".equals(fileNames)) {
            promise.fail("-1");
            return;
        }

        channelSftp.cd(remotePath);
        for (String fileName : fileNames.split(",")) {
            channelSftp.put(new FileInputStream(fileName), FileNameUtil.getName(fileName));
        }

        promise.complete();
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}