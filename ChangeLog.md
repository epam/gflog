# Change Log

## 3.0.5

- Support SLF4J 2.0.+.

## 3.0.4

- Support timestamp/date/time in nanoseconds.

## 3.0.3

- Support Java 17 (replaced usage of internal api to get address of direct ByteBuffer).

## 3.0.2

- Fixed the issue with JulBridge.install() [#6](https://github.com/epam/gflog/issues/6).
- Updated Gradle from 6.8.2 to 7.1.1.
- Updated JMH from 1.27 to 1.32.
- Updated JOL from 0.14 to 0.16.
- Updated Log4j from 2.14.0 to 2.14.1.
- Updated Disruptor from 3.4.2 to 3.4.4.
- Updated ThreadAffinity from 3.21ea1 to 3.21ea5.

## 3.0.1

- Added checkstyle plugin. Code cleanup.
- Improved performance by introducing an exception path to pass exceptions to the background thread.
- Improved performance by using FAA instead of CAS when claiming space for a log entry.

## 3.0.0

- Moved to github.
