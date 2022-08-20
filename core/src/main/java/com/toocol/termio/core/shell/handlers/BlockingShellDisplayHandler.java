package com.toocol.termio.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.shell.ShellAddress;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.sync.SharedCountdownLatch;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:44
 */
@SuppressWarnings("all")
public final class BlockingShellDisplayHandler extends BlockingMessageHandler<Long> implements Loggable {

    private final SshSessionCache.Instance sshSessionCache = SshSessionCache.Instance;
    private final ShellCache.Instance shellCache = ShellCache.Instance;

    private volatile boolean cmdHasFeedbackWhenJustExit = false;

    private volatile long firstIn = 0;

    public BlockingShellDisplayHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return ShellAddress.DISPLAY_SHELL;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Long> promise, @NotNull Message<T> message) throws Exception {
        long sessionId = cast(message.body());

        Shell shell = shellCache.getShell(sessionId);

        if (shell.hasWelcome() && StatusCache.SHOW_WELCOME) {
            shell.printWelcome();
            StatusCache.SHOW_WELCOME = false;
        }

        if (StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT) {
            Printer.print(shell.getPrompt());
        } else {
            StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT = true;
        }

        /*
         * All the remote feedback data is getting from this InputStream.
         * And don't know why, there should get a new InputStream from channelShell.
         **/
        InputStream in = shell.getInputStream();
        byte[] tmp = new byte[1024];

        while (true) {
            while (in.available() > 0) {
                if (StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE) {
                    continue;
                }
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }

                String msg = new String(tmp, 0, i, StandardCharsets.UTF_8);
                boolean hasPrint = shell.print(new String(new StringBuilder(msg)));
                if (hasPrint) {
                    int[] position = Term.instance.getCursorPosition();
                }
                if (hasPrint && StatusCache.JUST_CLOSE_EXHIBIT_SHELL) {
                    cmdHasFeedbackWhenJustExit = true;
                }
                SharedCountdownLatch.countdown(BlockingShellExecuteHandler.class, this.getClass());
            }

            if (StatusCache.HANGED_QUIT) {
                if (in.available() > 0) {
                    continue;
                }
                break;
            }
            if (shell.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                break;
            }
            if (StatusCache.JUST_CLOSE_EXHIBIT_SHELL) {
                if (firstIn == 0) {
                    firstIn = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - firstIn >= 2000) {
                        if (in.available() > 0) {
                            continue;
                        }
                        firstIn = 0;
                        break;
                    }
                }

                if (cmdHasFeedbackWhenJustExit) {
                    if (in.available() > 0) {
                        continue;
                    }
                    firstIn = 0;
                    break;
                }
            }
            /*
             * Reduce CPU utilization
             */
            Thread.sleep(1);
        }

        promise.complete(sessionId);
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Long> asyncResult, @NotNull Message<T> message) throws Exception {
        if (StatusCache.JUST_CLOSE_EXHIBIT_SHELL) {
            StatusCache.JUST_CLOSE_EXHIBIT_SHELL = false;
            cmdHasFeedbackWhenJustExit = false;
            SharedCountdownLatch.countdown(BlockingExecuteCmdInShellHandler.class, this.getClass());
            return;
        }
        if (StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING) {
            Long sessionId = asyncResult.result();
            ChannelShell channelShell = SshSessionCache.Instance.getChannelShell(sessionId);
            if (channelShell != null && !channelShell.isClosed()) {
                eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId);
            }
        }
    }
}
