package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;


public class TcpAppenderFactory extends NioAppenderFactory {

    protected static final long CONNECT_TIMEOUT = PropertyUtil.getDuration("gflog.tcp.appender.connect.timeout", TimeUnit.MILLISECONDS, 5 * 1000);
    protected static final long RECONNECT_INITIAL_PERIOD = PropertyUtil.getDuration("gflog.tcp.appender.reconnect.initial.period", TimeUnit.MILLISECONDS, 5 * 1000);
    protected static final long RECONNECT_MAX_PERIOD = PropertyUtil.getDuration("gflog.tcp.appender.reconnect.max.period", TimeUnit.MILLISECONDS, 10 * 60 * 1000);
    protected static final long SEND_TIMEOUT = PropertyUtil.getDuration("gflog.tcp.appender.send.timeout", TimeUnit.MILLISECONDS, 5 * 1000);

    protected static final int SOCKET_SEND_BUFFER_CAPACITY = PropertyUtil.getMemory("gflog.tcp.appender.socket.send.buffer.capacity", 2 * 1024 * 1024);
    protected static final int SOCKET_RECEIVE_BUFFER_CAPACITY = PropertyUtil.getMemory("gflog.tcp.appender.socket.receive.buffer.capacity", 0);
    protected static final boolean SOCKET_TCP_NO_DELAY = PropertyUtil.getBoolean("gflog.tcp.appender.socket.tcp.no.delay", false);

    protected String host;
    protected int port = -1;

    protected long connectTimeout = CONNECT_TIMEOUT;
    protected long reconnectInitialPeriod = RECONNECT_INITIAL_PERIOD;
    protected long reconnectMaxPeriod = RECONNECT_MAX_PERIOD;
    protected long sendTimeout = SEND_TIMEOUT;

    protected int socketSendBufferCapacity = SOCKET_SEND_BUFFER_CAPACITY;
    protected int socketReceiveBufferCapacity = SOCKET_RECEIVE_BUFFER_CAPACITY;
    protected boolean socketTcpNoDelay = SOCKET_TCP_NO_DELAY;

    public TcpAppenderFactory(final String defaultName) {
        super(defaultName);
    }

    public TcpAppenderFactory() {
        super("tcp");
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setConnectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout.toMillis();
    }

    public void setReconnectInitialPeriod(final Duration reconnectInitialPeriod) {
        this.reconnectInitialPeriod = reconnectInitialPeriod.toMillis();
    }

    public void setReconnectMaxPeriod(final Duration reconnectMaxPeriod) {
        this.reconnectMaxPeriod = reconnectMaxPeriod.toMillis();
    }

    public void setSendTimeout(final Duration sendTimeout) {
        this.sendTimeout = sendTimeout.toMillis();
    }

    public void setSocketSendBufferCapacity(final int socketSendBufferCapacity) {
        this.socketSendBufferCapacity = socketSendBufferCapacity;
    }

    public void setSocketReceiveBufferCapacity(final int socketReceiveBufferCapacity) {
        this.socketReceiveBufferCapacity = socketReceiveBufferCapacity;
    }

    public void setSocketTcpNoDelay(final boolean socketTcpNoDelay) {
        this.socketTcpNoDelay = socketTcpNoDelay;
    }

    @Override
    protected void conclude() {
        super.conclude();

        requireNonNull(host, "host is null");

        if (port < 0) {
            throw new IllegalArgumentException("Port is not specified or invalid " + port);
        }

        if (connectTimeout < 0) {
            connectTimeout = 0;
        }

        if (reconnectInitialPeriod < 0) {
            reconnectInitialPeriod = 1;
        }

        if (reconnectMaxPeriod < reconnectInitialPeriod) {
            reconnectMaxPeriod = reconnectInitialPeriod;
        }

        if (sendTimeout < 0) {
            sendTimeout = 0;
        }
    }

    @Override
    protected TcpAppender createAppender() {
        return new TcpAppender(
                name,
                level,
                bufferCapacity,
                flushCapacity,
                layout,
                host,
                port,
                connectTimeout,
                reconnectInitialPeriod,
                reconnectMaxPeriod,
                sendTimeout,
                socketSendBufferCapacity,
                socketReceiveBufferCapacity,
                socketTcpNoDelay
        );
    }

}
