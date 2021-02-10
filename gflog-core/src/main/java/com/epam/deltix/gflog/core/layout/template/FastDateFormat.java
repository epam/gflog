package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.util.Formatting;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>FastDateFormat is a fast char buffer implementation similar to
 * {@link java.text.SimpleDateFormat}.</p>
 * <p>
 * <p>Only formatting is supported, but all patterns are compatible with
 * SimpleDateFormat (except time zones - see below).</p>
 * <p>
 * <p>Java 1.4 introduced a new pattern letter, <code>'Z'</code>, to represent
 * time zones in RFC822 format (eg. <code>+0800</code> or <code>-1100</code>).
 * This pattern letter can be used here (on all JDK versions).</p>
 * <p>
 * <p>In addition, the pattern <code>'ZZ'</code> has been made to represent
 * ISO8601 full format time zones (eg. <code>+08:00</code> or <code>-11:00</code>).
 * This introduces a minor incompatibility with Java 1.4, but at a gain of
 * useful functionality.</p>
 *
 * @author TeaTrove project
 * @author Brian S O'Neill
 * @author Sean Schofield
 * @author Gary Gregory
 * @author Stephen Colebourne
 * @author Nikolay Metchev
 * @author Vladimir Dolzhenko
 * @version $Id: FastDateFormat.java 590552 2007-10-31 04:04:32Z bayard $
 * @since 2.0
 */
@SuppressWarnings("ALL")
final class FastDateFormat {


    private static String cDefaultPattern;

    private static final Map<FastDateFormat, FastDateFormat> cInstanceCache = new HashMap<FastDateFormat, FastDateFormat>(7);
    private static final Map<Object, FastDateFormat> cDateInstanceCache = new HashMap<Object, FastDateFormat>(7);
    private static final Map<Object, FastDateFormat> cTimeInstanceCache = new HashMap<Object, FastDateFormat>(7);
    private static final Map<Object, FastDateFormat> cDateTimeInstanceCache = new HashMap<Object, FastDateFormat>(7);
    private static final Map<Object, String> cTimeZoneDisplayCache = new HashMap<Object, String>(7);

    private final Calendar mCalendar;

    /**
     * The pattern.
     */
    private final String mPattern;
    /**
     * The time zone.
     */
    private final TimeZone mTimeZone;
    /**
     * Whether the time zone overrides any on Calendars.
     */
    private final boolean mTimeZoneForced;
    /**
     * The locale.
     */
    private final Locale mLocale;
    /**
     * Whether the locale overrides the default.
     */
    private final boolean mLocaleForced;
    /**
     * The parsed rules.
     */
    private transient Rule[] mRules;
    /**
     * The estimated maximum length.
     */
    private transient int mMaxLengthEstimate;

    //-----------------------------------------------------------------------

    /**
     * <p>Gets a formatter instance using the default pattern in the
     * default locale.</p>
     *
     * @return a date/time formatter
     */
    public static FastDateFormat getInstance() {
        return getInstance(getDefaultPattern(), null, null);
    }

    /**
     * <p>Gets a formatter instance using the specified pattern, time zone
     * and locale.</p>
     *
     * @param pattern  {@link java.text.SimpleDateFormat} compatible
     *                 pattern
     * @param timeZone optional time zone, overrides time zone of
     *                 formatted date
     * @param locale   optional locale, overrides system locale
     * @return a pattern based date/time formatter
     * @throws IllegalArgumentException if pattern is invalid
     *                                  or <code>null</code>
     */
    public static synchronized FastDateFormat getInstance(String pattern, TimeZone timeZone, Locale locale) {
        FastDateFormat emptyFormat = new FastDateFormat(pattern, timeZone, locale);
        FastDateFormat format = cInstanceCache.get(emptyFormat);
        if (format == null) {
            format = emptyFormat;
            format.init();  // convert shell format into usable one
            cInstanceCache.put(format, format);  // this is OK!
        }
        return format;
    }

    /**
     * <p>Gets a date formatter instance using the specified style, time
     * zone and locale.</p>
     *
     * @param style    date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone optional time zone, overrides time zone of
     *                 formatted date
     * @param locale   optional locale, overrides system locale
     * @return a localized standard date formatter
     * @throws IllegalArgumentException if the Locale has no date
     *                                  pattern defined
     */
    public static synchronized FastDateFormat getDateInstance(int style, TimeZone timeZone, Locale locale) {
        Object key = new Integer(style);
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        key = new Pair(key, locale);

        FastDateFormat format = cDateInstanceCache.get(key);
        if (format == null) {
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance(style, locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date pattern for locale: " + locale);
            }
        }
        return format;
    }

    /**
     * <p>Gets a time formatter instance using the specified style, time
     * zone and locale.</p>
     *
     * @param style    time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone optional time zone, overrides time zone of
     *                 formatted time
     * @param locale   optional locale, overrides system locale
     * @return a localized standard time formatter
     * @throws IllegalArgumentException if the Locale has no time
     *                                  pattern defined
     */
    public static synchronized FastDateFormat getTimeInstance(int style, TimeZone timeZone, Locale locale) {
        Object key = new Integer(style);
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }
        if (locale != null) {
            key = new Pair(key, locale);
        }

        FastDateFormat format = cTimeInstanceCache.get(key);
        if (format == null) {
            if (locale == null) {
                locale = Locale.getDefault();
            }

            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getTimeInstance(style, locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cTimeInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date pattern for locale: " + locale);
            }
        }
        return format;
    }

    /**
     * <p>Gets a date/time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *                  formatted date
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *                                  pattern defined
     */
    public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone,
                                                                  Locale locale) {

        Object key = new Pair(new Integer(dateStyle), new Integer(timeStyle));
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        key = new Pair(key, locale);

        FastDateFormat format = cDateTimeInstanceCache.get(key);
        if (format == null) {
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                        locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateTimeInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return format;
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Gets the time zone display name, using a cache for performance.</p>
     *
     * @param tz       the zone to query
     * @param daylight true if daylight savings
     * @param style    the style to use <code>TimeZone.LONG</code>
     *                 or <code>TimeZone.SHORT</code>
     * @param locale   the locale to use
     * @return the textual name of the time zone
     */
    static synchronized String getTimeZoneDisplay(TimeZone tz, boolean daylight, int style, Locale locale) {
        // xxx :: GARBAGE !!!
        Object key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        String value = cTimeZoneDisplayCache.get(key);
        if (value == null) {
            // This is a very slow call, so cache the results.
            value = tz.getDisplayName(daylight, style, locale);
            cTimeZoneDisplayCache.put(key, value);
        }
        return value;
    }

    /**
     * <p>Gets the default pattern.</p>
     *
     * @return the default pattern
     */
    private static synchronized String getDefaultPattern() {
        if (cDefaultPattern == null) {
            cDefaultPattern = new SimpleDateFormat().toPattern();
        }
        return cDefaultPattern;
    }

    // Constructor
    //-----------------------------------------------------------------------

    /**
     * <p>Constructs a new FastDateFormat.</p>
     *
     * @param pattern  {@link java.text.SimpleDateFormat} compatible
     *                 pattern
     * @param timeZone time zone to use, <code>null</code> means use
     *                 default for <code>Date</code> and value within for
     *                 <code>Calendar</code>
     * @param locale   locale, <code>null</code> means use system
     *                 default
     * @throws IllegalArgumentException if pattern is invalid or
     *                                  <code>null</code>
     */
    protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
        if (pattern == null) {
            throw new IllegalArgumentException("The pattern must not be null");
        }
        mPattern = pattern;

        // enforce time zone
        //mTimeZoneForced = (timeZone != null);
        mTimeZoneForced = true;
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        mTimeZone = timeZone;

        // enforce locale
        //mLocaleForced = (locale != null);
        mLocaleForced = true;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        mLocale = locale;

        mCalendar = new GregorianCalendar();
        mCalendar.setTimeZone(mTimeZone);
    }

    /**
     * <p>Initializes the instance for first use.</p>
     */
    protected void init() {
        List<Rule> rulesList = parsePattern();
        mRules = rulesList.toArray(new Rule[rulesList.size()]);

        int len = 0;
        for (int i = mRules.length; --i >= 0; ) {
            len += mRules[i].estimateLength();
        }

        mMaxLengthEstimate = len;
    }

    // Parse the pattern
    //-----------------------------------------------------------------------

    /**
     * <p>Returns a list of Rules given a pattern.</p>
     *
     * @return a <code>List</code> of Rule objects
     * @throws IllegalArgumentException if pattern is invalid
     */
    protected List<Rule> parsePattern() {
        DateFormatSymbols symbols = new DateFormatSymbols(mLocale);
        List<Rule> rules = new ArrayList<Rule>();

        String[] ERAs = symbols.getEras();
        String[] months = symbols.getMonths();
        String[] shortMonths = symbols.getShortMonths();
        String[] weekdays = symbols.getWeekdays();
        String[] shortWeekdays = symbols.getShortWeekdays();
        String[] AmPmStrings = symbols.getAmPmStrings();

        int length = mPattern.length();
        int[] indexRef = new int[1];

        for (int i = 0; i < length; i++) {
            indexRef[0] = i;
            String token = parseToken(mPattern, indexRef);
            i = indexRef[0];

            int tokenLen = token.length();
            if (tokenLen == 0) {
                break;
            }

            Rule rule;
            char c = token.charAt(0);

            switch (c) {
                case 'G': // era designator (text)
                    rule = new TextField(Calendar.ERA, ERAs);
                    break;
                case 'y': // year (number)
                    if (tokenLen >= 4) {
                        rule = selectNumberRule(Calendar.YEAR, tokenLen);
                    } else {
                        rule = TwoDigitYearField.INSTANCE;
                    }
                    break;
                case 'M': // month in year (text and number)
                    if (tokenLen >= 4) {
                        rule = new TextField(Calendar.MONTH, months);
                    } else if (tokenLen == 3) {
                        rule = new TextField(Calendar.MONTH, shortMonths);
                    } else if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                    } else {
                        rule = UnpaddedMonthField.INSTANCE;
                    }
                    break;
                case 'd': // day in month (number)
                    rule = selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
                    break;
                case 'h': // hour in am/pm (number, 1..12)
                    rule = new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));
                    break;
                case 'H': // hour in day (number, 0..23)
                    rule = selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
                    break;
                case 'm': // minute in hour (number)
                    rule = selectNumberRule(Calendar.MINUTE, tokenLen);
                    break;
                case 's': // second in minute (number)
                    rule = selectNumberRule(Calendar.SECOND, tokenLen);
                    break;
                case 'S': // millisecond (number)
                    rule = selectSubSecondField(Calendar.MILLISECOND, tokenLen);
                    break;
                case 'E': // day in week (text)
                    rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
                    break;
                case 'D': // day in year (number)
                    rule = selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
                    break;
                case 'F': // day of week in month (number)
                    rule = selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
                    break;
                case 'w': // week in year (number)
                    rule = selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
                    break;
                case 'W': // week in month (number)
                    rule = selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
                    break;
                case 'a': // am/pm marker (text)
                    rule = new TextField(Calendar.AM_PM, AmPmStrings);
                    break;
                case 'k': // hour in day (1..24)
                    rule = new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
                    break;
                case 'K': // hour in am/pm (0..11)
                    rule = selectNumberRule(Calendar.HOUR, tokenLen);
                    break;
                case 'z': // time zone (text)
                    if (tokenLen >= 4) {
                        rule = new TimeZoneNameRule(mTimeZone, mTimeZoneForced, mLocale, TimeZone.LONG);
                    } else {
                        rule = new TimeZoneNameRule(mTimeZone, mTimeZoneForced, mLocale, TimeZone.SHORT);
                    }
                    break;
                case 'Z': // time zone (value)
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                    } else {
                        rule = TimeZoneNumberRule.INSTANCE_COLON;
                    }
                    break;
                case '\'': // literal text
                    String sub = token.substring(1);
                    if (sub.length() == 1) {
                        rule = new CharacterLiteral(sub.charAt(0));
                    } else {
                        rule = new StringLiteral(sub);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal pattern component: " + token);
            }

            rules.add(rule);
        }

        return rules;
    }

    /**
     * <p>Performs the parsing of tokens.</p>
     *
     * @param pattern  the pattern
     * @param indexRef index references
     * @return parsed token
     */
    protected String parseToken(String pattern, int[] indexRef) {
        StringBuffer buf = new StringBuffer();

        int i = indexRef[0];
        int length = pattern.length();

        char c = pattern.charAt(i);
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            // Scan a run of the same character, which indicates a time
            // pattern.
            buf.append(c);

            while (i + 1 < length) {
                char peek = pattern.charAt(i + 1);
                if (peek == c) {
                    buf.append(c);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            // This will identify token as text.
            buf.append('\'');

            boolean inLiteral = false;

            for (; i < length; i++) {
                c = pattern.charAt(i);

                if (c == '\'') {
                    if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                        // '' is treated as escaped '
                        i++;
                        buf.append(c);
                    } else {
                        inLiteral = !inLiteral;
                    }
                } else if (!inLiteral &&
                        (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    i--;
                    break;
                } else {
                    buf.append(c);
                }
            }
        }

        indexRef[0] = i;
        return buf.toString();
    }

    /**
     * <p>Gets an appropriate rule for the padding required.</p>
     *
     * @param field   the field to get a rule for
     * @param padding the padding required
     * @return a new rule with the correct padding
     */
    protected NumberRule selectNumberRule(int field, int padding) {
        switch (padding) {
            case 1:
                return new UnpaddedNumberField(field);
            case 2:
                return new TwoDigitNumberField(field);
            default:
                return new PaddedNumberField(field, padding);
        }
    }

    protected NumberRule selectSubSecondField(int field, int padding) {
        switch (padding) {
            case 6:
                return new MicrosField();
            case 9:
                return new NanosField();
            default:
                return selectNumberRule(field, padding);
        }
    }

    public ByteBuffer format(long millis, ByteBuffer buf) {
        mCalendar.setTimeInMillis(millis);
        return applyRules(mCalendar, buf);
    }

    public ByteBuffer formatNanos(long nanos, ByteBuffer buf) {
        long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
        mCalendar.setTimeInMillis(millis);
        return applyRules(mCalendar, buf, nanos);
    }

    protected ByteBuffer applyRules(Calendar calendar, ByteBuffer buffer) {
        Rule[] rules = mRules;
        int len = mRules.length;
        for (int i = 0; i < len; i++) {
            rules[i].appendTo(buffer, calendar);
        }
        return buffer;
    }

    protected ByteBuffer applyRules(Calendar calendar, ByteBuffer buffer, long nanos) {
        Rule[] rules = mRules;
        int len = mRules.length;
        for (int i = 0; i < len; i++) {
            rules[i].appendTo(buffer, calendar, nanos);
        }
        return buffer;
    }

    // Accessors
    //-----------------------------------------------------------------------

    /**
     * <p>Gets the pattern used by this formatter.</p>
     *
     * @return the pattern, {@link java.text.SimpleDateFormat} compatible
     */
    public String getPattern() {
        return mPattern;
    }

    /**
     * <p>Gets an estimate for the maximum string length that the
     * formatter will produce.</p>
     * <p>
     * <p>The actual formatted length will almost always be less than or
     * equal to this amount.</p>
     *
     * @return the maximum formatted length
     */
    public int getMaxLengthEstimate() {
        return mMaxLengthEstimate;
    }

    // Basics
    //-----------------------------------------------------------------------

    /**
     * <p>Compares two objects for equality.</p>
     *
     * @param obj the object to compare to
     * @return <code>true</code> if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FastDateFormat == false) {
            return false;
        }
        FastDateFormat other = (FastDateFormat) obj;
        if (
                (mPattern == other.mPattern || mPattern.equals(other.mPattern)) &&
                        (mTimeZone == other.mTimeZone || mTimeZone.equals(other.mTimeZone)) &&
                        (mLocale == other.mLocale || mLocale.equals(other.mLocale)) &&
                        (mTimeZoneForced == other.mTimeZoneForced) &&
                        (mLocaleForced == other.mLocaleForced)
        ) {
            return true;
        }
        return false;
    }

    /**
     * <p>Returns a hashcode compatible with equals.</p>
     *
     * @return a hashcode compatible with equals
     */
    @Override
    public int hashCode() {
        int total = 0;
        total += mPattern.hashCode();
        total += mTimeZone.hashCode();
        total += (mTimeZoneForced ? 1 : 0);
        total += mLocale.hashCode();
        total += (mLocaleForced ? 1 : 0);
        return total;
    }


    /**
     * <p>Inner class defining a rule.</p>
     */
    private interface Rule {
        /**
         * Returns the estimated lentgh of the result.
         *
         * @return the estimated length
         */
        int estimateLength();

        void appendTo(ByteBuffer buffer, Calendar calendar);

        default void appendTo(ByteBuffer buffer, Calendar calendar, long nanos) {
            appendTo(buffer, calendar);
        }
    }

    /**
     * <p>Inner class defining a numeric rule.</p>
     */
    private interface NumberRule extends Rule {
        /**
         * Appends the specified value to the output buffer based on the rule implementation.
         *
         * @param buffer the output buffer
         * @param value  the value to be appended
         */
        void appendTo(ByteBuffer buffer, int value);
    }

    /**
     * <p>Inner class to output a constant single character.</p>
     */
    private static class CharacterLiteral implements Rule {
        private final char mValue;

        /**
         * Constructs a new instance of <code>CharacterLiteral</code>
         * to hold the specified value.
         *
         * @param value the character literal
         */
        CharacterLiteral(char value) {
            mValue = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            BufferFormatter.append(buffer, mValue);
        }
    }

    /**
     * <p>Inner class to output a constant string.</p>
     */
    private static class StringLiteral implements Rule {
        private final String mValue;

        /**
         * Constructs a new instance of <code>StringLiteral</code>
         * to hold the specified value.
         *
         * @param value the string literal
         */
        StringLiteral(String value) {
            mValue = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return mValue.length();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            BufferFormatter.append(buffer, mValue);
        }
    }

    /**
     * <p>Inner class to output one of a set of values.</p>
     */
    private static class TextField implements Rule {
        private final int mField;
        private final String[] mValues;

        /**
         * Constructs an instance of <code>TextField</code>
         * with the specified field and values.
         *
         * @param field  the field
         * @param values the field values
         */
        TextField(int field, String[] values) {
            mField = field;
            mValues = values;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            int max = 0;
            for (int i = mValues.length; --i >= 0; ) {
                int len = mValues[i].length();
                if (len > max) {
                    max = len;
                }
            }
            return max;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            BufferFormatter.append(buffer, mValues[calendar.get(mField)]);
        }

    }

    /**
     * <p>Inner class to output an unpadded number.</p>
     */
    private static class UnpaddedNumberField implements NumberRule {

        private final int mField;

        /**
         * Constructs an instance of <code>UnpadedNumberField</code> with the specified field.
         *
         * @param field the field
         */
        UnpaddedNumberField(int field) {
            mField = field;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 4;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            if (value < 10) {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else if (value < 100) {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else {
                BufferFormatter.append(buffer, value);
            }
        }
    }

    /**
     * <p>Inner class to output an unpadded month.</p>
     */
    private static class UnpaddedMonthField implements NumberRule {
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        /**
         * Constructs an instance of <code>UnpaddedMonthField</code>.
         */
        UnpaddedMonthField() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            if (value < 10) {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            }
        }
    }

    /**
     * <p>Inner class to output a padded number.</p>
     */
    private static class PaddedNumberField implements NumberRule {
        private final int mField;
        private final int mSize;

        /**
         * Constructs an instance of <code>PaddedNumberField</code>.
         *
         * @param field the field
         * @param size  size of the output field
         */
        PaddedNumberField(int field, int size) {
            if (size < 3) {
                // Should use UnpaddedNumberField or TwoDigitNumberField.
                throw new IllegalArgumentException();
            }
            mField = field;
            mSize = size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 4;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        @Override
        public final void appendTo(ByteBuffer buffer, int value) {
            if (value < 100) {
                for (int i = mSize; --i >= 2; ) {
                    BufferFormatter.append(buffer, '0');
                }
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else {
                int digits;
                if (value < 1000) {
                    digits = 3;
                } else {
                    if (!(value > -1)) {
                        throw new IllegalArgumentException("Negative values should not be possible" + value);
                    }
                    digits = Formatting.lengthOfUInt(value);
                }
                for (int i = mSize; --i >= digits; ) {
                    BufferFormatter.append(buffer, '0');
                }
                BufferFormatter.append(buffer, value);
            }
        }
    }

    private static class NanosField implements NumberRule {

        public static final NanosField INSTANCE = new NanosField();

        private static final int LENGTH = 9;

        @Override
        public int estimateLength() {
            return LENGTH;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            throw new UnsupportedOperationException("appendTo(buffer, calendar)");
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar, long nanos) {
            appendTo(buffer, (int) (nanos % 1_000_000_000));
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            int size = Formatting.lengthOfUInt(value);
            for (int i = 0; i < LENGTH - size; i++) {
                BufferFormatter.append(buffer, '0');
            }

            BufferFormatter.append(buffer, value);
        }

    }

    private static class MicrosField implements NumberRule {

        public static final NanosField INSTANCE = new NanosField();

        private static final int LENGTH = 6;

        @Override
        public int estimateLength() {
            return LENGTH;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            throw new UnsupportedOperationException("appendTo(buffer, calendar)");
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar, long nanos) {
            int micros = (int) (nanos / 1_000 % 1_000_000);
            appendTo(buffer, micros);
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            int size = Formatting.lengthOfUInt(value);
            for (int i = 0; i < LENGTH - size; i++) {
                BufferFormatter.append(buffer, '0');
            }

            BufferFormatter.append(buffer, value);
        }

    }

    /**
     * <p>Inner class to output a two digit number.</p>
     */
    private static class TwoDigitNumberField implements NumberRule {
        private final int mField;

        /**
         * Constructs an instance of <code>TwoDigitNumberField</code> with the specified field.
         *
         * @param field the field
         */
        TwoDigitNumberField(int field) {
            mField = field;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            if (value < 100) {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else {
                BufferFormatter.append(buffer, value);
            }
        }
    }

    /**
     * <p>Inner class to output a two digit year.</p>
     */
    private static class TwoDigitYearField implements NumberRule {
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        /**
         * Constructs an instance of <code>TwoDigitYearField</code>.
         */
        TwoDigitYearField() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
        }
    }

    /**
     * <p>Inner class to output a two digit month.</p>
     */
    private static class TwoDigitMonthField implements NumberRule {
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        /**
         * Constructs an instance of <code>TwoDigitMonthField</code>.
         */
        TwoDigitMonthField() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
        }
    }

    /**
     * <p>Inner class to output the twelve hour field.</p>
     */
    private static class TwelveHourField implements NumberRule {
        private final NumberRule mRule;

        /**
         * Constructs an instance of <code>TwelveHourField</code> with the specified
         * <code>NumberRule</code>.
         *
         * @param rule the rule
         */
        TwelveHourField(NumberRule rule) {
            mRule = rule;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            int value = calendar.get(Calendar.HOUR);
            if (value == 0) {
                value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * <p>Inner class to output the twenty four hour field.</p>
     */
    private static class TwentyFourHourField implements NumberRule {
        private final NumberRule mRule;

        /**
         * Constructs an instance of <code>TwentyFourHourField</code> with the specified
         * <code>NumberRule</code>.
         *
         * @param rule the rule
         */
        TwentyFourHourField(NumberRule rule) {
            mRule = rule;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            int value = calendar.get(Calendar.HOUR_OF_DAY);
            if (value == 0) {
                value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(ByteBuffer buffer, int value) {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * <p>Inner class to output a time zone name.</p>
     */
    private static class TimeZoneNameRule implements Rule {
        private final TimeZone mTimeZone;
        private final boolean mTimeZoneForced;
        private final Locale mLocale;
        private final int mStyle;
        private final String mStandard;
        private final String mDaylight;

        /**
         * Constructs an instance of <code>TimeZoneNameRule</code> with the specified properties.
         *
         * @param timeZone       the time zone
         * @param timeZoneForced if <code>true</code> the time zone is forced into standard and daylight
         * @param locale         the locale
         * @param style          the style
         */
        TimeZoneNameRule(TimeZone timeZone, boolean timeZoneForced, Locale locale, int style) {
            mTimeZone = timeZone;
            mTimeZoneForced = timeZoneForced;
            mLocale = locale;
            mStyle = style;

            if (timeZoneForced) {
                mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
                mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
            } else {
                mStandard = null;
                mDaylight = null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            if (mTimeZoneForced) {
                return Math.max(mStandard.length(), mDaylight.length());
            } else if (mStyle == TimeZone.SHORT) {
                return 4;
            } else {
                return 40;
            }
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            if (mTimeZoneForced) {
                if (mTimeZone.useDaylightTime() && calendar.get(Calendar.DST_OFFSET) != 0) {
                    BufferFormatter.append(buffer, mDaylight);
                } else {
                    BufferFormatter.append(buffer, mStandard);
                }
            } else {
                TimeZone timeZone = calendar.getTimeZone();
                if (timeZone.useDaylightTime() && calendar.get(Calendar.DST_OFFSET) != 0) {
                    BufferFormatter.append(buffer, getTimeZoneDisplay(timeZone, true, mStyle, mLocale));
                } else {
                    BufferFormatter.append(buffer, getTimeZoneDisplay(timeZone, false, mStyle, mLocale));
                }
            }
        }
    }

    /**
     * <p>Inner class to output a time zone as a number <code>+/-HHMM</code>
     * or <code>+/-HH:MM</code>.</p>
     */
    private static class TimeZoneNumberRule implements Rule {
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);

        final boolean mColon;

        /**
         * Constructs an instance of <code>TimeZoneNumberRule</code> with the specified properties.
         *
         * @param colon add colon between HH and MM in the output if <code>true</code>
         */
        TimeZoneNumberRule(boolean colon) {
            mColon = colon;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int estimateLength() {
            return 5;
        }

        @Override
        public void appendTo(ByteBuffer buffer, Calendar calendar) {
            int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

            if (offset < 0) {
                BufferFormatter.append(buffer, '-');
                offset = -offset;
            } else {
                BufferFormatter.append(buffer, '+');
            }

            int hours = offset / (60 * 60 * 1000);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[hours]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[hours]);

            if (mColon) {
                BufferFormatter.append(buffer, ':');
            }

            int minutes = offset / (60 * 1000) - 60 * hours;
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[minutes]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[minutes]);
        }
    }

    // ----------------------------------------------------------------------

    /**
     * <p>Inner class that acts as a compound key for time zone names.</p>
     */
    private static class TimeZoneDisplayKey {
        private final TimeZone mTimeZone;
        private final int mStyle;
        private final Locale mLocale;

        /**
         * Constructs an instance of <code>TimeZoneDisplayKey</code> with the specified properties.
         *
         * @param timeZone the time zone
         * @param daylight adjust the style for daylight saving time if <code>true</code>
         * @param style    the timezone style
         * @param locale   the timezone locale
         */
        TimeZoneDisplayKey(TimeZone timeZone,
                           boolean daylight, int style, Locale locale) {
            mTimeZone = timeZone;
            if (daylight) {
                style |= 0x80000000;
            }
            mStyle = style;
            mLocale = locale;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return mStyle * 31 + mLocale.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeZoneDisplayKey) {
                TimeZoneDisplayKey other = (TimeZoneDisplayKey) obj;
                return
                        mTimeZone.equals(other.mTimeZone) &&
                                mStyle == other.mStyle &&
                                mLocale.equals(other.mLocale);
            }
            return false;
        }
    }

    // ----------------------------------------------------------------------

    /**
     * <p>Helper class for creating compound objects.</p>
     * <p>
     * <p>One use for this class is to create a hashtable key
     * out of multiple objects.</p>
     */
    private static class Pair {
        private final Object mObj1;
        private final Object mObj2;

        /**
         * Constructs an instance of <code>Pair</code> to hold the specified objects.
         *
         * @param obj1 one object in the pair
         * @param obj2 second object in the pair
         */
        public Pair(Object obj1, Object obj2) {
            mObj1 = obj1;
            mObj2 = obj2;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Pair)) {
                return false;
            }

            Pair key = (Pair) obj;

            return
                    (mObj1 == null ?
                            key.mObj1 == null : mObj1.equals(key.mObj1)) &&
                            (mObj2 == null ?
                                    key.mObj2 == null : mObj2.equals(key.mObj2));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return
                    (mObj1 == null ? 0 : mObj1.hashCode()) +
                            (mObj2 == null ? 0 : mObj2.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "[" + mObj1 + ':' + mObj2 + ']';
        }
    }

    private static class BufferFormatter {

        public final static char[] DIGIT_TENS = {
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
                '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
                '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
                '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
                '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
                '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
                '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
                '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
                '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        };

        public final static char[] DIGIT_ONES = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        };

        /**
         * All possible chars for representing a number as a String
         */
        public final static char[] DIGITS = {
                '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f', 'g', 'h',
                'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z'
        };

        public static void append(final ByteBuffer buffer, CharSequence s) {
            append(buffer, s, 0, s != null ? s.length() : 0);
        }

        public static void append(final ByteBuffer buffer, CharSequence s, int start, int end) {
            if (s == null) {
                assert !(buffer.remaining() < 4);
                buffer.put((byte) 'n').put((byte) 'u').put((byte) 'l').put((byte) 'l');
            } else {
                assert !(buffer.remaining() < (end - start));
                for (int i = start; i < end; i++) {
                    buffer.put((byte) s.charAt(i));
                }
            }
        }

        public static void append(final ByteBuffer buffer, char i) {
            buffer.put((byte) i);
        }

        public static void append(final ByteBuffer buffer, int i) {
            if (i == Integer.MIN_VALUE) {
                // uses java.lang.Integer string constant of MIN_VALUE
                append(buffer, Integer.toString(i));
            } else {
                put(buffer, i);
            }
        }

        // based on java.lang.Integer.getChars(int i, int index, char[] buf)
        private static void put(final ByteBuffer buffer, int i) {
            int x = -i;
            int size = (i < 0) ? Formatting.lengthOfUInt(x) + 1 : Formatting.lengthOfUInt(i);

            assert !(buffer.remaining() < size);

            int q, r;
            int charPos = size;

            int oldPos = buffer.position();

            char sign = 0;

            if (i < 0) {
                sign = '-';
                i = -i;
            }

            // Generate two digits per iteration
            while (i >= 65536) {
                q = i / 100;
                // really: r = i - (q * 100);
                r = i - ((q << 6) + (q << 5) + (q << 2));
                i = q;
                putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
                putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
            }

            // Fall thru to fast mode for smaller numbers
            // assert(i <= 65536, i);
            for (; ; ) {
                // 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1
                // 52429 = 32768 + 16384 + 2048 + 1024 + 128 + 64 + 8 + 4 + 1
                /*/
                q = ((i << 15) + (i << 14) + (i << 11) + (i << 10) + (i << 7) + (i << 6) + (i << 3) + (i << 2) + i) >> (16 + 3);
                /*/
                q = (i * 52429) >>> (16 + 3);
                //*/
                r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
                putAt(buffer, oldPos + (--charPos), DIGITS[r]);
                i = q;
                if (i == 0) break;
            }

            if (sign != 0) {
                putAt(buffer, oldPos + (--charPos), sign);
            }

            buffer.position(oldPos + size);
        }

        private static void putAt(final ByteBuffer buffer, int pos, char b) {
            buffer.position(pos);
            buffer.put((byte) b);
        }

        public static void append(ByteBuffer buffer, Object object) {
            append(buffer, object == null ? "null" : object.toString());
        }


    }
}
