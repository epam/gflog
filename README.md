Garbage Free Log 
=====

![Continuous Integration](https://github.com/epam/GFLog/workflows/Continuous%20Integration/badge.svg?branch=main)

High efficient garbage free log for Java 8+.

# Artifacts

### Gradle
```gradle
 api              'com.epam.deltix:gflog-api:3.0.0'   // only this dependency should be used in library modules
 
 implementation   'com.epam.deltix:gflog-core:3.0.0'  // if you want to program custom gflog configuration 
 runtimeOnly      'com.epam.deltix:gflog-core:3.0.0'  // if you just want to see logging in your app or tests, but don't want to leak this dependecy anywhere
 
 runtimeOnly      'com.epam.deltix:gflog-slf4j:3.0.0' // SLF4J bridge
 runtimeOnly      'com.epam.deltix:gflog-jcl:3.0.0'   // Apache Commons Logging bridge
 
 implementation   'com.epam.deltix:gflog-jul:3.0.0'   // Java Util Logging bridge, DO NOT FORGET TO CALL JulBridge.install()
```

# API (gflog-api)

### String Builder Style
Less convenient but more efficient. Programmer is responsible for committing log entry. 

```java
log.info()
   .append("Integer ").append(123456)
   .append(", String ").append("some")
   .append(", Timestamp ").appendTimestamp(System.currentTimeMillis()).append(" ms")
   .append(", Decimal ").appendDecimal64(0x2FFFFFFFFFFFFFFFL)
   .commit();
```

### String Format Style
More convenient but less efficient. Parses the pattern on each logging operation. Commits the log entry automatically.
```java
Log log = LogFactory.getLog(Sample.class);

log.info("Integer %s, String %s, Timestamp %s ms, Decimal %s")
   .with(123456)
   .with("some")
   .withTimestamp(System.currentTimeMillis())
   .withDecimal64(0x2FFFFFFFFFFFFFFFL);
```

# Implementation (gflog-core)

### Default configuration
Default configuration is initialized on the first use of gflog. The following order will be applied:
 * It tries to load the configuration from **gflog.config** property if specified.
 * It tries to load the configuration **gflog-test.xml** from the classpath if found.
 * It tries to load the configuration **gflog.xml** from the classpath if found.
 * It uses the default console configuration if any of the previous steps failed.

**Property**           | **Default** | **Description**
---------------------- | ----------- | -----------
**gflog.config**       |             | Config to use by default. Use "classpath:" prefix to load config from classpath.
**gflog.sync**         |    false    | Use sync log service in default console configuration. Used only if **gflog.config** is not specified.

### Programming configuration
Use **com.epam.deltix.gflog.core.LogConfigurator** to configure/unconfigure gflog.

**Method**                                                         | **Description**
------------------------------------------------------------------ | -----------
**void configure(String url)**                                     | Configures gflog from file. Uses system properties to substitute ${} in file. **Programmer is responsible for unconfiguring.**
**void configure(String url, Properties properties)**              | Configures gflog from file. Uses properties to substitute ${} in file. **Programmer is responsible for unconfiguring.**
**void configure(LogConfig config)**                               | Configures gflog from config DSL. **Programmer is responsible for unconfiguring.**
**void configureWithShutdown(LogConfig config)**                   | The same as above. But adds shutdown hook to unconfigure gflog. Use it if you don't need to control gflog's lifecycle.
**void unconfigure()**                                             | Unconfigures gflog.

Use **com.epam.deltix.gflog.core.LogConfigFactory** to load configuration and modify it.

**Method**                                                         | **Description**
------------------------------------------------------------------ | -----------
**LogConfig load(String url)**                                     | Loads configuration from file. Uses system properties to substitute ${substitution} in file.
**LogConfig load(String url, Properties properties)**              | Loads configuration from file. Uses properties to substitute ${substitution} in file.

Use **com.epam.deltix.gflog.core.LogConfig** to build configuration.

```java
LogServiceFactory service = new AsyncLogServiceFactory();  // log service is responsible for all operations between logs and appenders
Appender appender = new ConsoleAppenderFactory().create(); // appender is responsible for writing/storing/sending log messages
Logger logger = new Logger(LogLevel.INFO, appender);       // logger is link between log and appenders

LogConfig config = new LogConfig();
config.setService(service);
config.addAppender(appender);
config.addLogger(logger);

LogConfigurator.configureWithShutdown(config);
```

### Simple XML config
```xml
<config>
    <appender name="consoleAppender" factory="com.epam.deltix.gflog.core.appender.ConsoleAppenderFactory"/>
    <appender name="fileAppender" factory="com.epam.deltix.gflog.core.appender.DailyRollingFileAppenderFactory" file="./log.txt"/>

    <logger>
        <appender-ref ref="consoleAppender"/>
        <appender-ref ref="fileAppender"/>
    </logger>
</config>
```

### Advanced XML config
```xml
<config>
    <appender name="consoleAppender" factory="com.epam.deltix.gflog.core.appender.ConsoleAppenderFactory" bufferCapacity="1M">
        <layout template="%d{d MMM HH:mm:ss.SSSSSSSSS} (${user.name}) %p '%c' [%t] %m%n" zoneId="UTC"/>
    </appender>

    <appender name="fileAppender" factory="com.epam.deltix.gflog.core.appender.DailyRollingFileAppenderFactory" file="./log.txt" bufferCapacity="8M" append="false" fileSuffixTemplate="-yyyy-MM-dd" zoneId="UTC"/>

    <appender name="safeAppender" factory="com.epam.deltix.gflog.core.appender.SafeAppenderFactory">
        <appender-ref ref="consoleAppender"/>
        <appender-ref ref="fileAppender"/>
    </appender>

    <logger name="foo.bar" level="WARN">
        <appender-ref ref="safeAppender"/>
    </logger>

    <logger>
        <appender-ref ref="consoleAppender"/>
        <appender-ref ref="fileAppender"/>
    </logger>

    <service entryEncoding="UTF-8" entryInitialCapacity="16K" entryMaxCapacity="128K" entryTruncationSuffix="TRNCTD>>" bufferCapacity="16M" overflowStrategy="DISCARD">
        <idle-strategy maxSpins="0" maxYields="10" minParkPeriod="1000" maxParkPeriod="100000"/>
    </service>
</config>
```

# Gray Log Integration
You can stream log messages to Gray Log by using com.epam.deltix.gflog.core.layout.GelfLayout and com.epam.deltix.gflog.core.appender.TcpAppender.

### Config
You can use the following config sample to configure GF Log:
```xml
<config>
    <appender name="gelf" factory="com.epam.deltix.gflog.core.appender.TcpAppenderFactory" host="10.10.87.126" port="11111">
        <layout factory="com.epam.deltix.gflog.core.layout.GelfLayoutFactory">
            <additional-fields>
                <entry key="key" value="value"/>
            </additional-fields>
        </layout>
    </appender>

    <logger>
        <appender-ref ref="gelf"/>
    </logger>

</config>
```

### Sample
A message sample from the config above will be:
```json
{  
   "version":"1.1",
   "level":4,
   "host":"localhost",
   "_key":"value",
   "short_message":"Hey there!",
   "_logger":"java.util.Object",
   "_thread":"main",
   "timestamp":1345.234567890
}\0x00
```

### Advanced
**com.epam.deltix.gflog.core.appender.TcpAppenderFactory** has the following settings:

| **Attribute** | **Required** | **Default**   | **Description** |
|----------------------------------|---|-------| --------------- |
| **host**                         | Y |     - | Remote host     |
| **port**                         | Y |     - | Remote port     |
| **connectTimeout**               | N |    5s | Connect timeout on startup. Application threads are blocked awaiting for the logger to be initialized |
| **reconnectInitialPeriod**       | N |    5s | Reconnect period is reset to this value on a successful connection attempt |
| **reconnectMaxPeriod**           | N |   10m | Reconnect period is doubled gradually up to this value on each consequent failed connection attempt |
| **sendTimeout**                  | N |    5s | Disconnects after this period if a message can't be sent to socket because of backpressure |
| **socketSendBufferCapacity**     | N |    2M | Socket send buffer capacity. 0 is OS's default |
| **socketReceiveBufferCapacity**  | N |     0 | Socket receive buffer capacity. 0 is OS's default |
| **socketTcpNoDelay**             | N | false | Socket tcp no delay. true is to enable Nagle's algorithm |

**com.epam.deltix.gflog.core.layout.GelfLayoutFactory** has the following settings:

| **Attribute** | **Requred** | **Default** | **Description** |
| ------ | ------ | ------ | ------ |
| **host** | N | local host | Host to use in GELF messages |


You can use **additional-fields** element to send extra fields in GELF messages:
```xml
<additional-fields>
    <entry key="container" value="MyContainer"/>
    <entry key="app" value="MyApp"/>
    <entry key="location" value="NY"/>
</additional-fields>
```

# High/Low Resolution Clocks (Deprecated)

You can place deltix:deltix-clock library on the classpath and use the following config to enable high (realtime) or
low (realtime-coarse) clock:

```xml
<config>
    <appender name="consoleAppender" factory="com.epam.deltix.gflog.core.appender.ConsoleAppenderFactory">
        <layout template="%d{yyyy-MM-dd HH:mm:ss.SSSSSSSSS} %p [%t] %m%n"/> 
    </appender>
   
    <logger>
        <appender-ref ref="fileAppender"/>
    </logger>

    <service>
        <clock resolution="HIGH"/> 
    </service>
</config>
```


