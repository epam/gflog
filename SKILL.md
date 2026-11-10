---
name: gflog
description: Add or update logging statements using GFLog library. Use when creating new log statements, refactoring existing logging code (JUL/SLF4J), or when you see string concatenation in log statements.
---

# GFLog logging

Add logging statements using our company standard GFLog library.

## Process
1. Identify logging statements that need creation or conversion
2. Replace string concatenation with a-la printf-style formatting (with only %s available for all types)
3. Apply appropriate formatter methods (withTimestamp, withDecimal64, etc.)
4. Add guard conditions for expensive operations
5. Verify log level consistency

## Guidelines
 
* When you see string concatenation in existing log statement you need to replace it with printf-like approach followed by GFLog. 

  For example: 
  
    LOGGER.log(Level.FINE, "Found " + list.size() + " instruments");

  should be 

    LOG.debug("Found %s instruments").with(list.size());

## Formatters by Data Type

**Timestamps** (epoch millis as `long`):
- Use: `.withTimestamp(value)`
- Example: `.withTimestamp(System.currentTimeMillis())`

**DFP-encoded money/quantities** (`long` representing Decimal64):
- Use: `.withDecimal64(value)`
- Example: `.withDecimal64(price)` for prices, `.withDecimal64(quantity)` for quantities

**Alphanumeric identifiers** (`@Alphanumeric long`):
- Use: `.withAlphanumeric(value)`
- Example: `.withAlphanumeric(instrumentId)` for encoded symbol/ID

**Regular values**:
- Use: `.with(value)`

## Quick Start

**Initialize logger** (at class level):

```java
protected static final Log LOG = LogFactory.getLog(CurrentClass.class);
```

**Basic logging**:

```java
LOG.info("Hello %s! Current time is %s")
   .with(name)
   .withTimestamp(System.currentTimeMillis());
```

**With guard for expensive operations**:

```java
if (LOG.isDebugEnabled()) {
    LOG.debug("Computed result: %s").with(expensiveComputation());
}
```

Here we need to make sure that function `expensiveComputation()` does not perform any logging of its own inside its body.
GFLog is not re-entrant. In such cases it is better to move function call outside of logging statement:


```java
if (LOG.isDebugEnabled()) {
    var x = expensiveComputation();
    LOG.debug("Computed result: %s").with(x);
}
```

## Typical pitfalls

* Only `%s` is supported as a format specifier — type-specific specifiers (`%d`, `%04d`, `%f`, etc.) are not recognized.
  * Incorrect: `LOG.info("Value is %04d").with(value)`
  * Correct: `LOG.info("Value is %s").with(value)`

* Every `.with*()` call must have a matching `%s` placeholder in the format string.
  * Incorrect: `LOG.error("Error").with(exception)`
  * Correct: `LOG.error("Error: %s").with(exception)`

* Use typed appenders instead of `.with()` for timestamps and DFP values:
  * Epoch millis → `.withTimestamp(value)`, epoch nanos → `.withTimestampNs(value)`
  * `Decimal64`-encoded `long` → `.withDecimal64(value)`
  * Incorrect: `LOG.info("Message time is %s").with(message.getTimestamp())`
  * Correct: `LOG.info("Message time is %s").withTimestamp(message.getTimestamp())`


