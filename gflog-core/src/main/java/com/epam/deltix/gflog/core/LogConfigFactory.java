package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.appender.AppenderFactory;
import com.epam.deltix.gflog.core.appender.CompositeAppenderFactory;
import com.epam.deltix.gflog.core.appender.ConsoleAppenderFactory;
import com.epam.deltix.gflog.core.service.LogServiceFactory;
import com.epam.deltix.gflog.core.util.Factory;
import com.epam.deltix.gflog.core.util.PropertyUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public final class LogConfigFactory {

    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String FILE_PREFIX = "file:";

    public static final String CONFIG = PropertyUtil.getString("gflog.config", null);
    public static final String CONFIG_SCHEMA = PropertyUtil.getString("gflog.config.schema", "gflog.xsd");

    @Deprecated
    public static final boolean CONFIG_VALIDATE = true;
    public static final boolean CONFIG_CLASSPATH_SEARCH = PropertyUtil.getBoolean("gflog.config.classpath.search", true);
    private static final String[] CONFIG_CLASSPATH_FILES = {"gflog-test.xml", "gflog.xml"};

    private static final String APPENDER = "appender";
    private static final String APPENDER_REF = "appender-ref";
    private static final String LOGGER = "logger";
    private static final String SERVICE = "service";
    private static final String ENTRY = "entry";

    private LogConfigFactory() {
    }

    public static LogConfig loadDefault() {
        LogConfig config = (CONFIG == null) ?
                loadDefaultFromClasspath() :
                loadDefaultFromProperty();

        if (config == null) {
            config = createDefault();
            LogDebug.debug("using default console config");
        }

        return config;
    }

    private static LogConfig loadDefaultFromClasspath() {
        LogConfig config = null;

        if (CONFIG_CLASSPATH_SEARCH) {
            URL url = null;

            for (final String file : CONFIG_CLASSPATH_FILES) {
                try {
                    url = Thread.currentThread().getContextClassLoader().getResource(file);

                    if (url != null) {
                        break;
                    }
                } catch (final Throwable e) {
                    LogDebug.warn("can't locate config on classpath: " + file, e);
                }
            }

            if (url != null) {
                try (final InputStream stream = url.openStream()) {
                    config = load(stream);
                    LogDebug.debug("using config from classpath: " + url);
                } catch (final Throwable e) {
                    LogDebug.warn("can't load config from classpath: " + url, e);
                }
            }
        }

        return config;
    }

    private static LogConfig loadDefaultFromProperty() {
        LogConfig config = null;

        try {
            config = load(CONFIG);
            LogDebug.debug("using config from gflog.config property: " + CONFIG);
        } catch (final Throwable e) {
            LogDebug.warn("can't load config from gflog.config property: " + CONFIG, e);
        }

        return config;
    }

    private static LogConfig createDefault() {
        final Appender appender = new ConsoleAppenderFactory().create();
        final Logger logger = new Logger(LogLevel.INFO, appender);

        final LogConfig config = new LogConfig();
        config.addAppender(appender);
        config.addLogger(logger);

        return config;
    }

    /**
     * Loads a config from the classpath if the url prefixed with "classpath:".
     * Otherwise loads a config from file if the url prefixed with "file:" or not prefixed.
     * Uses the system properties for substitutions.
     *
     * @param url to load from.
     * @return the log config.
     * @throws Exception if failed to load.
     */
    public static LogConfig load(final String url) throws Exception {
        return load(url, System.getProperties());
    }

    /**
     * Loads a config from the classpath if the url prefixed with "classpath:".
     * Otherwise loads a config from file if the url prefixed with "file:" or not prefixed.
     * Uses the specified properties for substitutions.
     *
     * @param url        to load from.
     * @param properties to use for substitutions.
     * @return the log config.
     * @throws Exception if failed.
     */
    public static LogConfig load(final String url, final Properties properties) throws Exception {
        final InputStream stream;

        if (url.startsWith(CLASSPATH_PREFIX)) {
            final String file = url.substring(CLASSPATH_PREFIX.length());
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        } else {
            String file = url;

            if (url.startsWith(FILE_PREFIX)) {
                file = url.substring(FILE_PREFIX.length());
            }

            stream = new FileInputStream(file);
        }

        try (final InputStream resource = stream) {
            return load(stream, properties);
        }
    }

    /**
     * Loads a config from the file. Uses the system properties for substitutions.
     *
     * @param file to load from.
     * @return the log config.
     * @throws Exception if failed.
     */
    public static LogConfig load(final File file) throws Exception {
        return load(file, System.getProperties());
    }

    /**
     * Loads a config from the file. Uses the specified properties for substitutions.
     *
     * @param file       to load from.
     * @param properties to use for substitutions.
     * @return the log config.
     * @throws Exception if failed.
     */
    public static LogConfig load(final File file, final Properties properties) throws Exception {
        try (final InputStream stream = new FileInputStream(file)) {
            return load(stream, properties);
        }
    }

    /**
     * Loads a config from the input stream. Uses the system properties for substitutions.
     *
     * @param stream to load from.
     * @return the log config.
     * @throws Exception if failed.
     */
    public static LogConfig load(final InputStream stream) throws Exception {
        return load(stream, System.getProperties());
    }

    /**
     * Loads a config from the input stream. Uses the specified properties for substitutions.
     *
     * @param stream     to load from.
     * @param properties to use for substitutions.
     * @return the log config.
     * @throws Exception if failed.
     */
    public static LogConfig load(final InputStream stream, final Properties properties) throws Exception {
        try (final InputStream substitution = substitute(stream, properties)) {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(Thread.currentThread().getContextClassLoader().getResource(CONFIG_SCHEMA));

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setSchema(schema);
            factory.setNamespaceAware(true);

            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(ErrorHandler.INSTANCE);

            final Document document = builder.parse(substitution);
            return getConfig(document);
        }
    }

    private static InputStream substitute(final InputStream stream, final Properties properties) throws Exception {
        byte[] array = new byte[Math.max(stream.available() + 1, 4096)];
        int length = 0;

        while (true) {
            final int read = stream.read(array, length, array.length - length);

            if (read < 0) {
                break;
            }

            length += read;

            if (length == array.length) {
                array = Arrays.copyOf(array, length << 1);
            }
        }

        final String original = new String(array, 0, length, StandardCharsets.UTF_8);
        final String substitution = PropertyUtil.substitute(original, properties);

        return (original == substitution) ?
                new ByteArrayInputStream(array, 0, length) :
                new ByteArrayInputStream(substitution.getBytes(StandardCharsets.UTF_8));
    }

    private static LogConfig getConfig(final Document document) throws Exception {
        final Element root = document.getDocumentElement();
        final LogConfig config = new LogConfig();

        addAppenders(root, config);
        addLoggers(root, config);
        setService(root, config);

        return config;
    }

    private static void addAppenders(final Element root, final LogConfig config) throws Exception {
        final NodeList elements = root.getElementsByTagName(APPENDER);

        for (int i = 0; i < elements.getLength(); i++) {
            final Element element = (Element) elements.item(i);
            final AppenderFactory factory = (AppenderFactory) instantiateFactory(element);

            final NodeList appenders = element.getElementsByTagName(APPENDER_REF);
            if (appenders.getLength() > 0) {
                addAppenderReferences(appenders, config, (CompositeAppenderFactory) factory);
            }

            final Appender appender = factory.create();
            config.addAppender(appender);
        }
    }

    private static void addAppenderReferences(final NodeList elements,
                                              final LogConfig config,
                                              final CompositeAppenderFactory factory) {

        for (int i = 0; i < elements.getLength(); i++) {
            final Element item = (Element) elements.item(i);
            final String name = item.getAttribute("ref");
            final Appender appender = config.getAppender(name);
            factory.addAppender(appender);
        }
    }

    private static void addLoggers(final Element root, final LogConfig config) {
        final NodeList elements = root.getElementsByTagName(LOGGER);

        for (int i = 0; i < elements.getLength(); i++) {
            final Element element = (Element) elements.item(i);
            final String name = element.getAttribute("name");
            final LogLevel level = getLogLevel(element);
            final Appender[] appenders = getAppenderReferences(element, config);

            final Logger logger = new Logger(name, level, appenders);
            config.addLogger(logger);
        }
    }

    private static LogLevel getLogLevel(final Element element) {
        return convertToEnum(LogLevel.class, element.getAttribute("level"));
    }

    private static Appender[] getAppenderReferences(final Element element, final LogConfig config) {
        final NodeList elements = element.getElementsByTagName(APPENDER_REF);
        final Appender[] appenders = new Appender[elements.getLength()];

        for (int i = 0; i < appenders.length; i++) {
            final Element item = (Element) elements.item(i);
            final String name = item.getAttribute("ref");
            appenders[i] = config.getAppender(name);
        }

        return appenders;
    }

    private static void setService(final Element root, final LogConfig config) throws Exception {
        final NodeList serviceElements = root.getElementsByTagName(SERVICE);

        if (serviceElements.getLength() > 0) {
            final Element element = (Element) serviceElements.item(0);
            final LogServiceFactory factory = (LogServiceFactory) instantiateFactory(element);
            config.setService(factory);
        }
    }

    private static Object instantiateFactory(final Element element) throws Exception {
        final String name = element.getAttribute("factory");
        final Class<?> clazz = Class.forName(name);
        final Object factory = clazz.newInstance();
        final PropertyDescriptor[] properties = Introspector.getBeanInfo(clazz).getPropertyDescriptors();

        invokeSettersByAttributes(properties, element, factory);
        invokeSettersByElements(properties, element, factory);

        return factory;
    }

    private static void invokeSettersByAttributes(final PropertyDescriptor[] properties, final Element element, final Object bean) throws Exception {
        for (final PropertyDescriptor property : properties) {
            final Method setter = property.getWriteMethod();

            if (setter != null) {
                final String propName = property.getName();

                if (element.hasAttribute(propName)) {
                    final Object value = convertAttribute(propName, property.getPropertyType(), element.getAttribute(propName));
                    setter.invoke(bean, value);
                }
            }
        }
    }

    private static void invokeSettersByElements(final PropertyDescriptor[] properties, final Element element, final Object bean) throws Exception {
        for (final PropertyDescriptor property : properties) {
            final Method setter = property.getWriteMethod();

            if (setter != null) {
                final String propName = property.getName();
                final String elementName = elementName(propName);

                final NodeList elements = element.getElementsByTagName(elementName);
                if (elements.getLength() > 0) {
                    final Element child = (Element) elements.item(0);
                    final Object value = convertElement(propName, property.getPropertyType(), child);
                    setter.invoke(bean, value);
                }
            }
        }
    }

    private static Object convertElement(final String propName, final Class<?> type, final Element element) throws Exception {
        if (Map.class.isAssignableFrom(type)) {
            return covertMap(element);
        }

        final Factory<?> childFactory = (Factory<?>) instantiateFactory(element);
        return childFactory.create();
    }

    private static Map<String, String> covertMap(final Element element) {
        final HashMap<String, String> map = new HashMap<>();
        final NodeList children = element.getElementsByTagName(ENTRY);

        for (int i = 0; i < children.getLength(); i++) {
            final Element item = (Element) children.item(i);

            final String key = item.getAttribute("key");
            final String value = item.getAttribute("value");

            map.put(key, value);
        }

        return map;
    }

    private static Object convertAttribute(final String name, final Class<?> type, final String value) throws Exception {
        try {
            return convertAttribute(type, value);
        } catch (final Exception e) {
            throw new AttributeConversionException(name, type, value, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object convertAttribute(final Class<?> type, final String value) throws Exception {
        if (type == String.class) {
            return value;
        }

        if (type.isPrimitive()) {
            return convertToPrimitive(type, value);
        }

        if (type.isEnum()) {
            return convertToEnum((Class<Enum>) type, value);
        }

        if (type == ZoneId.class) {
            return ZoneId.of(value);
        }

        if (type == Duration.class) {
            return PropertyUtil.toDuration(value);
        }

        throw new IllegalArgumentException("unsupported type: " + type);
    }

    private static Object convertToPrimitive(final Class<?> type, final String value) {
        if (type == boolean.class) {
            return PropertyUtil.toBoolean(value);
        }

        if (type == long.class) {
            return PropertyUtil.toMemory(value);
        }

        if (type == int.class) {
            return Math.toIntExact(PropertyUtil.toMemory(value));
        }

        if (type == short.class) {
            return Short.parseShort(value);
        }

        if (type == byte.class) {
            return Byte.parseByte(value);
        }

        if (type == double.class) {
            return Double.parseDouble(value);
        }

        if (type == float.class) {
            return Float.parseFloat(value);
        }

        throw new IllegalArgumentException("unsupported primitive type: " + type);
    }

    private static <T extends Enum<T>> T convertToEnum(final Class<T> type, final String value) {
        return Enum.valueOf(type, value);
    }

    private static String elementName(final String propName) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < propName.length(); i++) {
            char c = propName.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append("-");
                c = Character.toLowerCase(c);
            }

            builder.append(c);
        }

        return builder.toString();
    }

    private static class AttributeConversionException extends SAXException {

        AttributeConversionException(final String name, final Class<?> type, final String value, final Exception cause) {
            super(
                    "Attribute (" + name + "=\"" + value + "\") conversion to type (" + type + ") failed",
                    cause
            );
        }

    }

    private static class ErrorHandler implements org.xml.sax.ErrorHandler {

        private static final ErrorHandler INSTANCE = new ErrorHandler();

        @Override
        public void warning(final SAXParseException exception) throws SAXException {
            LogDebug.warn(exception.getMessage());
        }

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            LogDebug.warn(exception.getMessage());
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            LogDebug.warn(exception.getMessage());
        }

    }

}
