package com.toocol.termio.core.shell.core;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.utilities.utils.Castable;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:48
 * @version: 0.0.1
 */
public final class ExecChannelProvider implements Castable {

    private static final ExecChannelProvider INSTANCE = new ExecChannelProvider();

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();

    public static ExecChannelProvider getInstance() {
        return INSTANCE;
    }

    public ChannelExec getChannelExec(Long sessionId) throws Exception {
        Session session = sshSessionCache.getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("Session is null, sessionId = " + sessionId);
        }
        if (!session.isConnected()) {
            throw new RuntimeException("Session is not connected, sessionId = " + sessionId);
        }
        return cast(session.openChannel("exec"));
    }

}
