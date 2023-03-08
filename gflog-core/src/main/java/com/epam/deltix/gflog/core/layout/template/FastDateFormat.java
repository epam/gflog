package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.util.Formatting;

import java.nio.ByteBuffer;
import java.text.DateFormatSymbols;
import java.util.*;
import java.util.concurrent.TimeUnit;


final class FastDateFormat {

    private static final Map<FastDateFormat, FastDateFormat> INSTANCES = new HashMap<>(7);
    private static final Map<Object, String> TIME_ZONES = new HashMap<>(7);

    private final Calendar mCalendar;

    private final String mPattern;
    private final TimeZone mTimeZone;
    private final boolean mTimeZoneForced;
    private final Locale mLocale;
    private final boolean mLocaleForced;
    private Rule[] mRules;
    private int mMaxLengthEstimate;

    public static synchronized FastDateFormat getInstance(final String pattern,
                                                          final TimeZone timeZone,
                                                          final Locale locale) {
        final FastDateFormat emptyFormat = new FastDateFormat(pattern, timeZone, locale);
        FastDateFormat format = INSTANCES.get(emptyFormat);

        if (format == null) {
            format = emptyFormat;
            format.init();  // convert shell format into usable one
            INSTANCES.put(format, format);  // this is OK!
        }

        return format;
    }

    private static synchronized String getTimeZoneDisplay(final TimeZone tz,
                                                          final boolean daylight,
                                                          final int style,
                                                          final Locale locale) {
        // xxx :: GARBAGE !!!
        final Object key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        String value = TIME_ZONES.get(key);

        if (value == null) {
            // This is a very slow call, so cache the results.
            value = tz.getDisplayName(daylight, style, locale);
            TIME_ZONES.put(key, value);
        }

        return value;
    }

    private FastDateFormat(final String pattern,
                           final TimeZone timeZone,
                           final Locale locale) {

        Objects.requireNonNull(pattern);
        Objects.requireNonNull(timeZone);
        Objects.requireNonNull(locale);

        mPattern = pattern;

        // enforce time zone
        //mTimeZoneForced = (timeZone != null);
        mTimeZoneForced = true;
        mTimeZone = timeZone;

        // enforce locale
        //mLocaleForced = (locale != null);
        mLocaleForced = true;
        mLocale = locale;

        mCalendar = new GregorianCalendar();
        mCalendar.setTimeZone(mTimeZone);
    }

    protected void init() {
        final List<Rule> rulesList = parsePattern();
        mRules = rulesList.toArray(new Rule[rulesList.size()]);

        int len = 0;
        for (int i = mRules.length; --i >= 0; ) {
            len += mRules[i].estimateLength();
        }

        mMaxLengthEstimate = len;
    }

    protected List<Rule> parsePattern() {
        final DateFormatSymbols symbols = new DateFormatSymbols(mLocale);
        final List<Rule> rules = new ArrayList<>();

        final String[] eras = symbols.getEras();
        final String[] months = symbols.getMonths();
        final String[] shortMonths = symbols.getShortMonths();
        final String[] weekdays = symbols.getWeekdays();
        final String[] shortWeekdays = symbols.getShortWeekdays();
        final String[] amPmStrings = symbols.getAmPmStrings();

        final int length = mPattern.length();
        final int[] indexRef = new int[1];

        for (int i = 0; i < length; i++) {
            indexRef[0] = i;
            final String token = parseToken(mPattern, indexRef);
            i = indexRef[0];

            final int tokenLen = token.length();
            if (tokenLen == 0) {
                break;
            }

            final char c = token.charAt(0);
            final Rule rule = createRule(
                    c,
                    token,
                    tokenLen,
                    eras,
                    months,
                    shortMonths,
                    weekdays,
                    shortWeekdays,
                    amPmStrings
            );

            rules.add(rule);
        }

        return rules;
    }

    private Rule createRule(final char c,
                            final String token,
                            final int tokenLen,
                            final String[] eras,
                            final String[] months,
                            final String[] shortMonths,
                            final String[] weekdays,
                            final String[] shortWeekdays,
                            final String[] amPmStrings) {
        switch (c) {
            case 'G': // era designator (text)
                return new TextField(Calendar.ERA, eras);

            case 'y': // year (number)
                return tokenLen >= 4 ?
                        selectNumberRule(Calendar.YEAR, tokenLen) :
                        TwoDigitYearField.INSTANCE;

            case 'M': // month in year (text and number)
                return tokenLen >= 4 ? new TextField(Calendar.MONTH, months) :
                        tokenLen == 3 ? new TextField(Calendar.MONTH, shortMonths) :
                                tokenLen == 2 ? TwoDigitMonthField.INSTANCE :
                                        UnpaddedMonthField.INSTANCE;

            case 'd': // day in month (number)
                return selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);

            case 'h': // hour in am/pm (number, 1..12)
                return new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));

            case 'H': // hour in day (number, 0..23)
                return selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);

            case 'm': // minute in hour (number)
                return selectNumberRule(Calendar.MINUTE, tokenLen);

            case 's': // second in minute (number)
                return selectNumberRule(Calendar.SECOND, tokenLen);

            case 'S': // millisecond (number)
                return selectSubSecondField(Calendar.MILLISECOND, tokenLen);

            case 'E': // day in week (text)
                return new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);

            case 'D': // day in year (number)
                return selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);

            case 'F': // day of week in month (number)
                return selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);

            case 'w': // week in year (number)
                return selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);

            case 'W': // week in month (number)
                return selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);

            case 'a': // am/pm marker (text)
                return new TextField(Calendar.AM_PM, amPmStrings);

            case 'k': // hour in day (1..24)
                return new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));

            case 'K': // hour in am/pm (0..11)
                return selectNumberRule(Calendar.HOUR, tokenLen);

            case 'z': // time zone (text)
                return (tokenLen >= 4) ?
                        new TimeZoneNameRule(mTimeZone, mTimeZoneForced, mLocale, TimeZone.LONG) :
                        new TimeZoneNameRule(mTimeZone, mTimeZoneForced, mLocale, TimeZone.SHORT);

            case 'Z': // time zone (value)
                return (tokenLen == 1) ?
                        TimeZoneNumberRule.INSTANCE_NO_COLON :
                        TimeZoneNumberRule.INSTANCE_COLON;

            case '\'': // literal text
                final String sub = token.substring(1);
                return (sub.length() == 1) ?
                        new CharacterLiteral(sub.charAt(0)) :
                        new StringLiteral(sub);

            default:
                throw new IllegalArgumentException("Illegal pattern component: " + token);
        }
    }

    private String parseToken(final String pattern, final int[] indexRef) {
        final StringBuilder builder = new StringBuilder();

        int i = indexRef[0];
        final int length = pattern.length();

        char c = pattern.charAt(i);
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            // Scan a run of the same character, which indicates a time
            // pattern.
            builder.append(c);

            while (i + 1 < length) {
                final char peek = pattern.charAt(i + 1);
                if (peek == c) {
                    builder.append(c);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            // This will identify token as text.
            builder.append('\'');

            boolean inLiteral = false;

            for (; i < length; i++) {
                c = pattern.charAt(i);

                if (c == '\'') {
                    if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                        // '' is treated as escaped '
                        i++;
                        builder.append(c);
                    } else {
                        inLiteral = !inLiteral;
                    }
                } else if (!inLiteral &&
                        (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    i--;
                    break;
                } else {
                    builder.append(c);
                }
            }
        }

        indexRef[0] = i;
        return builder.toString();
    }

    protected NumberRule selectNumberRule(final int field, final int padding) {
        switch (padding) {
            case 1:
                return new UnpaddedNumberField(field);
            case 2:
                return new TwoDigitNumberField(field);
            default:
                return new PaddedNumberField(field, padding);
        }
    }

    protected NumberRule selectSubSecondField(final int field, final int padding) {
        switch (padding) {
            case 6:
                return new MicrosField();
            case 9:
                return new NanosField();
            default:
                return selectNumberRule(field, padding);
        }
    }

    public ByteBuffer format(final long millis, final ByteBuffer buf) {
        mCalendar.setTimeInMillis(millis);
        return applyRules(mCalendar, buf);
    }

    public ByteBuffer formatNanos(final long nanos, final ByteBuffer buf) {
        final long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
        mCalendar.setTimeInMillis(millis);
        return applyRules(mCalendar, buf, nanos);
    }

    protected ByteBuffer applyRules(final Calendar calendar, final ByteBuffer buffer) {
        final Rule[] rules = mRules;
        final int len = mRules.length;
        for (int i = 0; i < len; i++) {
            rules[i].appendTo(buffer, calendar);
        }
        return buffer;
    }

    protected ByteBuffer applyRules(final Calendar calendar, final ByteBuffer buffer, final long nanos) {
        final Rule[] rules = mRules;
        final int len = mRules.length;
        for (int i = 0; i < len; i++) {
            rules[i].appendTo(buffer, calendar, nanos);
        }
        return buffer;
    }

    public int getMaxLengthEstimate() {
        return mMaxLengthEstimate;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FastDateFormat)) {
            return false;
        }
        final FastDateFormat other = (FastDateFormat) obj;
        return Objects.equals(mPattern, other.mPattern) &&
                Objects.equals(mTimeZone, other.mTimeZone) &&
                Objects.equals(mLocale, other.mLocale) &&
                (mTimeZoneForced == other.mTimeZoneForced) &&
                (mLocaleForced == other.mLocaleForced);
    }

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

    private interface Rule {

        int estimateLength();

        void appendTo(ByteBuffer buffer, Calendar calendar);

        default void appendTo(ByteBuffer buffer, Calendar calendar, long nanos) {
            appendTo(buffer, calendar);
        }
    }

    private interface NumberRule extends Rule {

        void appendTo(ByteBuffer buffer, int value);
    }

    private static class CharacterLiteral implements Rule {

        private final char mValue;

        CharacterLiteral(char value) {
            mValue = value;
        }

        @Override
        public int estimateLength() {
            return 1;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            BufferFormatter.append(buffer, mValue);
        }

    }

    private static class StringLiteral implements Rule {

        private final String mValue;

        StringLiteral(final String value) {
            mValue = value;
        }

        @Override
        public int estimateLength() {
            return mValue.length();
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            BufferFormatter.append(buffer, mValue);
        }

    }

    private static class TextField implements Rule {

        private final int mField;
        private final String[] mValues;

        TextField(final int field, final String[] values) {
            mField = field;
            mValues = values;
        }

        @Override
        public int estimateLength() {
            int max = 0;

            for (int i = mValues.length; --i >= 0; ) {
                final int len = mValues[i].length();

                if (len > max) {
                    max = len;
                }
            }

            return max;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            BufferFormatter.append(buffer, mValues[calendar.get(mField)]);
        }

    }

    private static class UnpaddedNumberField implements NumberRule {

        private final int mField;

        UnpaddedNumberField(final int field) {
            mField = field;
        }

        @Override
        public int estimateLength() {
            return 4;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
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

    private static class UnpaddedMonthField implements NumberRule {

        private static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        UnpaddedMonthField() {
            super();
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            if (value >= 10) {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
            }

            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
        }

    }

    private static class PaddedNumberField implements NumberRule {

        private final int mField;
        private final int mSize;

        PaddedNumberField(final int field, final int size) {
            if (size < 3) {
                // Should use UnpaddedNumberField or TwoDigitNumberField.
                throw new IllegalArgumentException();
            }
            mField = field;
            mSize = size;
        }

        @Override
        public int estimateLength() {
            return 4;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        @Override
        public final void appendTo(final ByteBuffer buffer, final int value) {
            if (value < 100) {
                for (int i = mSize; --i >= 2; ) {
                    BufferFormatter.append(buffer, '0');
                }
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else {
                final int digits = value < 1000 ? 3 : Formatting.lengthOfUInt(value);

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
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            throw new UnsupportedOperationException("appendTo(buffer, calendar)");
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar, final long nanos) {
            appendTo(buffer, (int) (nanos % 1_000_000_000));
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            final int size = Formatting.lengthOfUInt(value);

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
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            throw new UnsupportedOperationException("appendTo(buffer, calendar)");
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar, final long nanos) {
            final int micros = (int) (nanos / 1_000 % 1_000_000);
            appendTo(buffer, micros);
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            final int size = Formatting.lengthOfUInt(value);

            for (int i = 0; i < LENGTH - size; i++) {
                BufferFormatter.append(buffer, '0');
            }

            BufferFormatter.append(buffer, value);
        }

    }

    private static class TwoDigitNumberField implements NumberRule {

        private final int mField;

        TwoDigitNumberField(final int field) {
            mField = field;
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(mField));
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            if (value < 100) {
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
                BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
            } else {
                BufferFormatter.append(buffer, value);
            }
        }

    }

    private static class TwoDigitYearField implements NumberRule {

        private static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
        }

    }

    private static class TwoDigitMonthField implements NumberRule {

        private static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        TwoDigitMonthField() {
            super();
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[value]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[value]);
        }

    }

    private static class TwelveHourField implements NumberRule {

        private final NumberRule mRule;

        TwelveHourField(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            int value = calendar.get(Calendar.HOUR);
            if (value == 0) {
                value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            mRule.appendTo(buffer, value);
        }

    }

    private static class TwentyFourHourField implements NumberRule {

        private final NumberRule mRule;

        TwentyFourHourField(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            int value = calendar.get(Calendar.HOUR_OF_DAY);
            if (value == 0) {
                value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final int value) {
            mRule.appendTo(buffer, value);
        }

    }

    private static class TimeZoneNameRule implements Rule {

        private final TimeZone mTimeZone;
        private final boolean mTimeZoneForced;
        private final Locale mLocale;
        private final int mStyle;
        private final String mStandard;
        private final String mDaylight;

        TimeZoneNameRule(final TimeZone timeZone, final boolean timeZoneForced, final Locale locale, final int style) {
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
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            if (mTimeZoneForced) {
                if (mTimeZone.useDaylightTime() && calendar.get(Calendar.DST_OFFSET) != 0) {
                    BufferFormatter.append(buffer, mDaylight);
                } else {
                    BufferFormatter.append(buffer, mStandard);
                }
            } else {
                final TimeZone timeZone = calendar.getTimeZone();
                if (timeZone.useDaylightTime() && calendar.get(Calendar.DST_OFFSET) != 0) {
                    BufferFormatter.append(buffer, getTimeZoneDisplay(timeZone, true, mStyle, mLocale));
                } else {
                    BufferFormatter.append(buffer, getTimeZoneDisplay(timeZone, false, mStyle, mLocale));
                }
            }
        }
    }

    private static class TimeZoneNumberRule implements Rule {

        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);

        final boolean mColon;

        TimeZoneNumberRule(final boolean colon) {
            mColon = colon;
        }

        @Override
        public int estimateLength() {
            return 5;
        }

        @Override
        public void appendTo(final ByteBuffer buffer, final Calendar calendar) {
            int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

            if (offset < 0) {
                BufferFormatter.append(buffer, '-');
                offset = -offset;
            } else {
                BufferFormatter.append(buffer, '+');
            }

            final int hours = offset / (60 * 60 * 1000);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[hours]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[hours]);

            if (mColon) {
                BufferFormatter.append(buffer, ':');
            }

            final int minutes = offset / (60 * 1000) - 60 * hours;
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_TENS[minutes]);
            BufferFormatter.append(buffer, BufferFormatter.DIGIT_ONES[minutes]);
        }
    }

    private static class TimeZoneDisplayKey {

        private final TimeZone mTimeZone;
        private final int mStyle;
        private final Locale mLocale;

        TimeZoneDisplayKey(final TimeZone timeZone,
                           final boolean daylight,
                           int style,
                           final Locale locale) {
            mTimeZone = timeZone;
            if (daylight) {
                style |= 0x80000000;
            }
            mStyle = style;
            mLocale = locale;
        }

        @Override
        public int hashCode() {
            return mStyle * 31 + mLocale.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeZoneDisplayKey) {
                final TimeZoneDisplayKey other = (TimeZoneDisplayKey) obj;
                return
                        mTimeZone.equals(other.mTimeZone) &&
                                mStyle == other.mStyle &&
                                mLocale.equals(other.mLocale);
            }
            return false;
        }
    }

    private static class BufferFormatter {

        public static final char[] DIGIT_TENS = {
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

        public static final char[] DIGIT_ONES = {
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

        public static final char[] DIGITS = {
                '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f', 'g', 'h',
                'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z'
        };

        public static void append(final ByteBuffer buffer, final CharSequence s) {
            append(buffer, s, 0, s != null ? s.length() : 0);
        }

        public static void append(final ByteBuffer buffer,
                                  final CharSequence s,
                                  final int start,
                                  final int end) {
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
            final int x = -i;
            final int size = (i < 0) ? Formatting.lengthOfUInt(x) + 1 : Formatting.lengthOfUInt(i);

            assert !(buffer.remaining() < size);

            int q, r;
            int charPos = size;

            final int oldPos = buffer.position();

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
            do {
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
            } while (i != 0);

            if (sign != 0) {
                putAt(buffer, oldPos + (--charPos), sign);
            }

            buffer.position(oldPos + size);
        }

        private static void putAt(final ByteBuffer buffer, int pos, char b) {
            buffer.position(pos);
            buffer.put((byte) b);
        }

        public static void append(final ByteBuffer buffer, final Object object) {
            append(buffer, object == null ? "null" : object.toString());
        }

    }

}
