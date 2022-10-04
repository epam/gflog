# Garbage Free Log 

[![GitHub](https://img.shields.io/badge/License-Apache--2.0-blue)](https://github.com/epam/GFLog/blob/main/LICENSE)
![Maven Central](https://img.shields.io/maven-central/v/com.epam.deltix/gflog-api)

![Continuous Integration](https://github.com/epam/GFLog/workflows/Continuous%20Integration/badge.svg?branch=main)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/epam/GFLog.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/epam/GFLog/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/epam/GFLog.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/epam/GFLog/alerts/)

Highly efficient garbage-free logging framework for Java 8+.

## Use

Add the following dependencies to your project:

```gradle
implementation 'com.epam.deltix:gflog-api:3.0.2'
runtimeOnly    'com.epam.deltix:gflog-core:3.0.2'
```

Use the following sample to log a message:

```java
Log log = LogFactory.getLog("my-logger");
log.info()
    .append("Hello world! This is a ")
    .append(LogLevel.INFO)
    .append(" message for you!")
    .commit();
```

Or:

```java
Log log = LogFactory.getLog("my-logger");
log.info("Hello world! This is a %s message for you!")
    .with(LogLevel.INFO);
```

## Reference

* [Bridges](https://github.com/epam/GFLog/wiki/Bridges)             - how to configure bridges for the existing logging frameworks.
* [Configuration](https://github.com/epam/GFLog/wiki/Configuration) - how to configure gflog.
* [XML Config](https://github.com/epam/gflog/wiki/XML-Config)       - how to configure gflog with a xml config.
* [Performance](https://github.com/epam/GFLog/wiki/Performance)     - what gflog's performance.

## Build

Build the project with Gradle and Java 8:
```
./gradlew build
```

## Acknowledgment
This project was inspired by [gflogger](https://github.com/vladimirdolzhenko/gflogger) library by Vladimir Dolzhenko. It originally started as a fork but further enhancements and new API made it into a separate project.

## License
 Copyright (C) 2022 EPAM

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

