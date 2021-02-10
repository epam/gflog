package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.layout.Layout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.LockSupport;


public class TcpAppender extends NioAppender<SocketChannel> {

    protected static final int STATE_DISCONNECTED = 0;
    protected static final int STATE_CONNECTING = 1;
    protected static final int STATE_CONNECTED = 2;

    protected final String host;
    protected final int port;

    protected final long connectTimeout;
    protected final long reconnectInitialPeriod;
    protected final long reconnectMaxPeriod;
    protected final long sendTimeout;

    protected final int socketSendBufferCapacity;
    protected final int socketReceiveBufferCapacity;
    protected final boolean socketTcpNoDelay;

    protected long connectTime = Long.MIN_VALUE;
    protected long reconnectPeriod;
    protected int state = STATE_DISCONNECTED;

    protected TcpAppender(final String name,
                          final LogLevel level,
                          final int bufferCapacity,
                          final int flushCapacity,
                          final Layout layout,
                          final String host,
                          final int port,
                          final long connectTimeout,
                          final long reconnectInitialPeriod,
                          final long reconnectMaxPeriod,
                          final long sendTimeout,
                          final int socketSendBufferCapacity,
                          final int socketReceiveBufferCapacity,
                          final boolean socketTcpNoDelay) {
        super(name, level, bufferCapacity, flushCapacity, layout);

        this.host = host;
        this.port = port;
        this.connectTimeout = connectTimeout;
        this.reconnectInitialPeriod = reconnectInitialPeriod;
        this.reconnectMaxPeriod = reconnectMaxPeriod;
        this.reconnectPeriod = reconnectInitialPeriod;
        this.sendTimeout = sendTimeout;
        this.socketSendBufferCapacity = socketSendBufferCapacity;
        this.socketReceiveBufferCapacity = socketReceiveBufferCapacity;
        this.socketTcpNoDelay = socketTcpNoDelay;
    }

    @Override
    public void open() {
        if (connectTimeout > 0) {
            final long deadline = System.currentTimeMillis() + connectTimeout;

            do {
                if (state == STATE_DISCONNECTED) {
                    initiateConnect();
                }

                if (state == STATE_CONNECTING) {
                    finishConnect();
                }

                if (state == STATE_CONNECTED) {
                    break;
                }

                LockSupport.parkNanos(16_000_000);
            } while (System.currentTimeMillis() < deadline);

            if (state != STATE_CONNECTED) {
                LogDebug.warn("can't connect to: " + host + ":" + port + " in specified connect timeout: " + connectTimeout);
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        state = STATE_DISCONNECTED;
    }

    @Override
    protected int flush(final boolean force) throws IOException {
        int workDone = 0;

        if (state != STATE_CONNECTED) {
            workDone += (state == STATE_DISCONNECTED) ? initiateConnect() : finishConnect();
        }

        workDone += super.flush(force);
        return workDone;
    }

    protected int initiateConnect() {
        final long now = System.currentTimeMillis();
        int workDone = 0;

        if (now >= connectTime + reconnectPeriod) {
            try {
                connectTime = now;
                state = STATE_CONNECTING;

                final InetSocketAddress address = new InetSocketAddress(host, port);

                channel = SocketChannel.open();
                channel.setOption(StandardSocketOptions.TCP_NODELAY, socketTcpNoDelay);

                if (socketSendBufferCapacity > 0) {
                    channel.setOption(StandardSocketOptions.SO_SNDBUF, socketSendBufferCapacity);
                }

                if (socketReceiveBufferCapacity > 0) {
                    channel.setOption(StandardSocketOptions.SO_RCVBUF, socketReceiveBufferCapacity);
                }

                channel.configureBlocking(false);
                channel.connect(address);

                workDone++;
            } catch (final Throwable e) {
                reconnectPeriod = Math.min(2 * reconnectPeriod, reconnectMaxPeriod);
                disconnect();
                LogDebug.warn("can't connect to: " + host + ":" + port + ". Error: " + e.getMessage());
            }
        }

        return workDone;
    }

    protected int finishConnect() {
        int workDone = 0;

        try {
            if (channel.finishConnect()) {
                state = STATE_CONNECTED;
                reconnectPeriod = reconnectInitialPeriod;
                workDone++;
            }
        } catch (final Throwable e) {
            reconnectPeriod = Math.min(2 * reconnectPeriod, reconnectMaxPeriod);
            disconnect();
            LogDebug.warn("can't connect to: " + host + ":" + port + ". Error: " + e.getMessage());
        }

        return workDone;
    }

    protected void disconnect() {
        closeChannel(channel);
        channel = null;
        state = STATE_DISCONNECTED;
    }

    protected int doFlush(final boolean force) {
        int bytesSent = 0;

        if (state == STATE_CONNECTED) {
            bytesSent = doSend(force);
        } else if (force) {
            offset = 0;
        }

        return bytesSent;
    }

    protected int doSend(final boolean force) {
        int bytesSent = 0;

        try {
            final int size = offset;

            byteBuffer.position(0);
            byteBuffer.limit(size);

            bytesSent = channel.write(byteBuffer);

            if (bytesSent < size && force) {
                final long deadline = System.currentTimeMillis() + sendTimeout;

                do {
                    if (System.currentTimeMillis() >= deadline) {
                        throw new IOException("Send timeout expired: " + sendTimeout);
                    }

                    final int sent = channel.write(byteBuffer);
                    bytesSent += sent;

                    if (sent == 0) {
                        LockSupport.parkNanos(1_000_000);
                    }
                } while (bytesSent < size);
            }

            if (bytesSent > 0) {
                final int remaining = size - bytesSent;

                if (remaining > 0) {
                    buffer.putBytes(0, buffer, bytesSent, remaining);
                }

                offset = remaining;
            }
        } catch (final Throwable e) {
            offset = 0;
            disconnect();
            LogDebug.warn("can't send messages to: " + host + ":" + port + ". Error: " + e.getMessage());
        }

        return bytesSent;
    }

    @Override
    protected SocketChannel openChannel() {
        throw new IllegalStateException("Should be not called");
    }

}
