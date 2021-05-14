package com.epam.deltix.gflog.mail.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.appender.NioAppender;
import com.epam.deltix.gflog.core.layout.Layout;
import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.Util;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.Properties;


public final class SmtpAppender extends NioAppender<WritableByteChannel> {

    private static final String SUBJECT_HYPHEN = ": ";

    private final SmtpSettings settings;
    private final RingBuffer recordSizes; // specified to the position after entry
    private final Trigger trigger;

    protected SmtpAppender(final String name,
                           final LogLevel level,
                           final int bufferCapacity,
                           final int flushCapacity,
                           final Layout layout,
                           final int maxEntries,
                           final LogLevel pushLevel,
                           final int period,
                           final int maxEmails,
                           final SmtpSettings settings) {
        super(name, level, bufferCapacity, flushCapacity, layout);

        this.settings = settings;
        this.recordSizes = new RingBuffer(maxEntries);
        this.trigger = new Trigger(pushLevel, period, maxEmails);
    }

    @Override
    public void open() {
        // skip
    }

    public void close() {
        // empty
    }

    @Override
    public int flush() {
        // empty
        return 0;
    }

    @Override
    public int append(final LogRecord record) {
        clearBufferIfNeeded(record);
        saveInBuffer(record);

        if (trigger.accept(record)) {
            sendBuffer(record);
        }

        return 1;
    }

    @Override
    protected WritableByteChannel openChannel() {
        return null;
    }

    private void sendBuffer(final LogRecord record) {
        byteBuffer.position(0);
        byteBuffer.limit(offset);

        try {
            sendMessage(byteBuffer, record, settings);
        } finally {
            offset = 0;
            recordSizes.clear();
        }
    }

    private void saveInBuffer(final LogRecord record) {
        final int before = offset;
        offset = layout.format(record, buffer, before);

        recordSizes.add(offset - before);
    }

    private void clearBufferIfNeeded(final LogRecord record) {
        final int size = layout.size(record);
        final int remaining = capacity - offset;

        if (remaining < size) {
            verifyRecordSize(size);

            int removed = 0;

            do {
                removed += recordSizes.remove();
            } while (remaining + removed < size);

            byteBuffer.limit(byteBuffer.position()).position(removed);
            byteBuffer.compact();

            offset -= removed;
            buffer.putBytes(0, buffer, removed, offset);
        }
    }

    // garbage
    private static void sendMessage(final ByteBuffer body, final LogRecord record, final SmtpSettings settings) {
        final Properties props = new Properties(System.getProperties());

        if (settings.getSecure() != null) {
            if (settings.getSecure().equals("STARTTLS")) {
                props.put("mail.smtp.starttls.enable", "true");
            } else if (settings.getSecure().equals("SSL")) {
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
        }

        props.put("mail.smtp.timeout", settings.getTimeout());

        if (settings.getHost() != null) {
            props.put("mail.smtp.host", settings.getHost());
        }

        if (settings.getPort() > 0) {
            props.put("mail.smtp.port", settings.getPort());
        }

        Authenticator auth = null;
        if (settings.getUsername() != null) {
            auth = new UsernamePasswordAuthenticator(settings.getUsername(), settings.getPassword());
            props.put("mail.smtp.user", settings.getUsername());
            props.put("mail.smtp.auth", "true");
        }

        final Session session = Session.getInstance(props, auth);
        session.setDebug(settings.isDebug());

        final MimeMessage message = new MimeMessage(session);

        try {
            if (settings.getFrom() != null) {
                message.setFrom(getAddress(settings.getFrom()));
            } else {
                message.setFrom();
            }

            message.setRecipients(Message.RecipientType.TO, parseAddress(settings.getTo()));
            message.setSubject(getSubject(settings.getSubject(), record, settings.getMaxSubjectLength()));

            final MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler(new DataHandler(new ByteBufferDataSource(body)));

            final Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            message.setContent(mp);

            message.setSentDate(new Date());

            // do send message
            Transport.send(message);
        } catch (final Exception ex) {
            LogDebug.warn("can't send email due to: " + ex.getMessage(), ex);
        }
    }

    private static String getSubject(final String prefix, final LogRecord record, final int maxLength) {
        final Buffer message = record.getMessage();

        int length = message.capacity();
        if (prefix != null) {
            length += prefix.length() + SUBJECT_HYPHEN.length();
        }

        length = Math.min(length, maxLength);

        final StringBuilder builder = new StringBuilder(length);
        if (prefix != null) {
            builder.append(prefix).append(SUBJECT_HYPHEN); // always entire
        }

        int i = 0;
        while (builder.length() < length) {
            builder.append((char) message.getByte(i++));
        }

        // use only first line in message
        final String lineSeparator = Util.LINE_SEPARATOR;
        i = builder.indexOf(lineSeparator);

        if (i != -1) {
            builder.delete(i, builder.length());
        }

        return builder.toString();
    }

    private static InternetAddress getAddress(final String address) {
        try {
            return new InternetAddress(address);
        } catch (final AddressException e) {
            LogDebug.warn("can't parse address [" + address + "].", e);
            return null;
        }
    }

    private static InternetAddress[] parseAddress(final String address) {
        try {
            return InternetAddress.parse(address, true);
        } catch (final AddressException e) {
            LogDebug.warn("can't parse address [" + address + "].", e);
            return new InternetAddress[0];
        }
    }

    private static final class UsernamePasswordAuthenticator extends Authenticator {

        private final PasswordAuthentication auth;

        private UsernamePasswordAuthenticator(final String user, final String password) {
            auth = new PasswordAuthentication(user, password);
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }

    }

    private static final class Trigger {

        private final LogLevel pushLevel;
        private final long periodMs;
        private final int maxEmailCount;

        private long periodStartTimeMs = Long.MIN_VALUE;
        private int counter;

        private Trigger(final LogLevel pushLevel, final int periodMs, final int maxEmailCount) {
            if (pushLevel == null) {
                throw new NullPointerException("pushLevel is null");
            }

            this.pushLevel = pushLevel;
            this.periodMs = periodMs;
            this.maxEmailCount = maxEmailCount;
        }

        public boolean accept(final LogRecord record) {
            if (!pushLevel.isLoggable(record.getLogLevel())) {
                return false;
            }

            if (periodMs <= 0) { // skip additional checks
                return true;
            }

            final long nowMs = System.currentTimeMillis();
            if (nowMs - periodMs < periodStartTimeMs) { // we are within period
                return maxEmailCount > 0 && ++counter < maxEmailCount;
            } else { // graceful period expired
                periodStartTimeMs = nowMs;
                counter = 0; // reset email counter
            }

            return true;
        }

    }

    private static final class ByteBufferDataSource implements DataSource {

        private static final String CONTENT_TYPE = "text/plain; charset=us-ascii";

        private final ByteBuffer buffer;

        private ByteBufferDataSource(final ByteBuffer buffer) {
            this.buffer = buffer.asReadOnlyBuffer();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteBufferInputStream(buffer);
        }

        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("getOutputStream");
        }

        @Override
        public String getContentType() {
            return CONTENT_TYPE;
        }

        @Override
        public String getName() {
            return null;
        }

        private static final class ByteBufferInputStream extends InputStream {

            private final ByteBuffer buffer;

            private ByteBufferInputStream(final ByteBuffer buffer) {
                this.buffer = buffer.duplicate();
            }

            @Override
            public int read() {
                if (!buffer.hasRemaining()) {
                    return -1;
                }

                return buffer.get();
            }

        }

    }

    private static final class RingBuffer {

        private final int[] values;
        private final int capacity;

        private int head;
        private int tail;
        private boolean empty = true;
        private boolean full = false;

        private RingBuffer(final int capacity) {
            if (capacity < 1) {
                throw new IllegalArgumentException("capacity < 1");
            }

            this.capacity = capacity;
            this.values = new int[capacity];
        }

        private void add(final int value) {
            values[head] = value;
            head = (head + 1) % capacity;

            if (empty) {
                empty = false;
            }

            if (full) {
                tail = head;
            } else {
                full = (head == tail);
            }
        }

        private int remove() {
            if (empty) {
                throw new IllegalStateException("empty");
            }

            final int value = values[tail];
            tail = (tail + 1) % capacity;

            if (full) {
                full = false;
            }

            empty = (tail == head);
            return value;
        }

        private void clear() {
            head = 0;
            tail = 0;
            empty = true;
            full = false;
        }

    }

}
