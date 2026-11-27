# Garbage Free Log 

[![GitHub](https://img.shields.io/badge/License-Apache--2.0-blue)](https://github.com/epam/GFLog/blob/main/LICENSE)
![Maven Central](https://img.shields.io/maven-central/v/com.epam.deltix/gflog-api)

![Continuous Integration](https://github.com/epam/GFLog/workflows/Continuous%20Integration/badge.svg?branch=main)
![CodeQL](https://github.com/epam/GFLog/workflows/CodeQL/badge.svg?branch=main)

Highly efficient garbage-free logging framework for Java 8 and above.

- Built and tested on Java 21
- Compiled with JDK 8, so the published jars run on Java 8 and newer
- Optimized formatting support for:
  - Epoch timestamps (milliseconds, nanoseconds)  
  - Alphanumeric values  
  - [DFP](https://github.com/epam/DFP/) decimal floating-point numbers  
- Asynchronous logging with configurable throttling and retention policies  
- Native support for streaming logs to Graylog using the [GELF](https://go2docs.graylog.org/current/getting_in_log_data/gelf.html) format

## Use

Add the following dependencies to your project:

```gradle
implementation 'com.epam.deltix:gflog-api:3.0.8'
runtimeOnly    'com.epam.deltix:gflog-core:3.0.8'
```

Use the following sample to log a message:

```java
Log log = LogFactory.getLog("my-logger");
...
log.info()
   .append("Hello ")
   .append(name)
   .append("! Current time is ")
   .appendTimestamp(System.currentTimeMillis())
   .commit();
```

Or:

```java
Log log = LogFactory.getLog("my-logger");
..
log.info("Hello %s! Current time is %s")
   .with(name)
   .withTimestamp(System.currentTimeMillis());
```

## Reference

* [Bridges](https://github.com/epam/GFLog/wiki/Bridges)             - how to configure bridges for the existing logging frameworks.
* [Configuration](https://github.com/epam/GFLog/wiki/Configuration) - how to configure gflog.
* [XML Config](https://github.com/epam/gflog/wiki/XML-Config)       - how to configure gflog with a xml config.
* [Performance](https://github.com/epam/GFLog/wiki/Performance)     - what gflog's performance.

## Build

The build uses Gradle 9 on Java 21. Compilation uses a JDK 8 toolchain, and Gradle downloads JDK 8 and JDK 21 if they are not already installed.

```
./gradlew build
```

## Acknowledgment
This project was inspired by [gflogger](https://github.com/vladimirdolzhenko/gflogger) library by Vladimir Dolzhenko. It originally started as a fork but further enhancements and new API made it into a separate project.

## License
 Copyright (C) 2026 EPAM

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

