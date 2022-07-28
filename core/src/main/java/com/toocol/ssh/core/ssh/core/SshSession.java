package com.toocol.ssh.core.ssh.core;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.utilities.functional.Switchable;

import java.util.Optional;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 15:32
 */
public class SshSession implements Switchable {

    private final long sessionId;
    private String host;
    private String user;
    private Session session;
    private ChannelShell channelShell;

    public SshSession(long sessionId) {
        this.sessionId = sessionId;
    }

    public SshSession(long sessionId, Session session, ChannelShell channelShell) {
        this.host = session.getHost();
        this.user = session.getUserName();
        this.sessionId = sessionId;
        this.session = session;
        this.channelShell = channelShell;
    }

    public synchronized void stop() {
        stopChannelShell();
        stopSession();
    }

    public synchronized void stopSession() {
        if (this.session == null) {
            return;
        }
        this.session.disconnect();
        this.session = null;
    }

    public synchronized void stopChannelShell() {
        if (channelShell == null) {
            return;
        }
        this.channelShell.disconnect();
        this.channelShell = null;
    }

    public Session getSession() {
        return session;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getHost() {
        return host;
    }

    public ChannelShell getChannelShell() {
        return channelShell;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setChannelShell(ChannelShell channelShell) {
        this.channelShell = channelShell;
    }

    @Override
    public String uri() {
        return user + "@" + host;
    }

    @Override
    public String protocol() {
        return ShellProtocol.SSH.name();
    }

    @Override
    public String currentPath() {
        return Optional.ofNullable(ShellCache.getInstance().getShell(sessionId)).map(Shell::getFullPath).orElse("*");
    }

    @Override
    public boolean alive() {
        return session.isConnected() && channelShell.isConnected();
    }

    @Override
    public int weight() {
        return 0;
    }
}