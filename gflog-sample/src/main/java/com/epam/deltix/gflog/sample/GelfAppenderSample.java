package com.epam.deltix.gflog.sample;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.core.LogConfigurator;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;


public class GelfAppenderSample {

    public static void main(final String[] args) throws Exception {
        launchServer();

        LogConfigurator.configure("classpath:gelf-appender.xml");
        Log log = LogFactory.getLog(ConfigSample.class);

        for (int i = 0; i < 100000; i++) {
            log.info("Hey there: %s!").with(i);
        }

        LogConfigurator.unconfigure();
    }

    private static void launchServer() {
        final Thread thread = new Thread("server") {
            @Override
            public void run() {
                try (final ServerSocketChannel server = ServerSocketChannel.open()) {
                    server.bind(new InetSocketAddress("localhost", 5555));

                    try (final SocketChannel input = server.accept();) {

                        final WritableByteChannel output = Channels.newChannel(System.err);
                        final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);

                        while (input.read(buffer) >= 0) {
                            buffer.flip();
                            prettify(buffer);

                            output.write(buffer);
                            buffer.clear();
                        }
                    }
                } catch (final Throwable e) {
                    e.printStackTrace(System.err);
                }
            }
        };
        thread.start();
    }

    private static void prettify(final ByteBuffer buffer) {
        for (int i = 0; i < buffer.limit(); i++) {
            final byte b = buffer.get(i);

            if (b == 0) {
                buffer.put(i, (byte) '\n');
            }
        }
    }

}
