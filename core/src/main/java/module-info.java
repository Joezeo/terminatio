module termio.core {
    requires termio.utilities;

    requires com.google.protobuf;
    requires com.google.common;
    requires io.vertx.core;
    requires jline;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires jsch;
    requires jdk.unsupported;
    requires jsr305;
    requires java.desktop;
    requires ini4j;

    exports com.toocol.termio.core;
    exports com.toocol.termio.core.cache;
    exports com.toocol.termio.core.config;
    exports com.toocol.termio.core.auth;
    exports com.toocol.termio.core.auth.core;
    exports com.toocol.termio.core.auth.handlers;
    exports com.toocol.termio.core.auth.vert;
    exports com.toocol.termio.core.file;
    exports com.toocol.termio.core.file.core;
    exports com.toocol.termio.core.file.handlers;
    exports com.toocol.termio.core.file.vert;
    exports com.toocol.termio.core.mosh;
    exports com.toocol.termio.core.mosh.core;
    exports com.toocol.termio.core.mosh.handlers;
    exports com.toocol.termio.core.mosh.vert;
    exports com.toocol.termio.core.shell;
    exports com.toocol.termio.core.shell.core;
    exports com.toocol.termio.core.shell.handlers;
    exports com.toocol.termio.core.shell.vert;
    exports com.toocol.termio.core.ssh;
    exports com.toocol.termio.core.ssh.core;
    exports com.toocol.termio.core.ssh.handlers;
    exports com.toocol.termio.core.ssh.vert;
    exports com.toocol.termio.core.term;
    exports com.toocol.termio.core.term.core;
    exports com.toocol.termio.core.term.handlers;
    exports com.toocol.termio.core.term.vert;
}