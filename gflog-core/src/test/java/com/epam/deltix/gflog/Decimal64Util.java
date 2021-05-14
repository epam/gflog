package com.epam.deltix.gflog;

import java.io.IOException;


@SuppressWarnings({"all"})
public class Decimal64Util {

    public static final long POSITIVE_INFINITY = 0x7800_0000_0000_0000L;
    public static final long NEGATIVE_INFINITY = 0xF800_0000_0000_0000L;
    public static final long NAN = 0x7C00_0000_0000_0000L;

    public static final long MAX_VALUE = 0x77FB_86F2_6FC0_FFFFL;
    public static final long MIN_VALUE = 0xF7FB_86F2_6FC0_FFFFL;

    public static long parse(final CharSequence value) {
        return parse(value, 0, value.length());
    }

    public static String format(final long value) {
        final StringBuilder builder = new StringBuilder();
        appendTo(value, builder);
        return builder.toString();

    }

    public static void appendTo(final long value, final StringBuilder builder) {
        try {
            appendTo(value, (Appendable) builder);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static final long MAX_COEFFICIENT = 9999_9999_9999_9999L;

    private static boolean isNaN(final long value) {
        return (value & MASK_INFINITY_NAN) == MASK_INFINITY_NAN;
    }

    private static boolean isSpecial(long value) {
        return (value & MASK_SPECIAL) == MASK_SPECIAL;
    }

    private static boolean isInfinity(final long value) {
        return (value & MASK_INFINITY_NAN) == POSITIVE_INFINITY;
    }


    private static boolean signBit(final long value) {
        return (value & MASK_SIGN) == MASK_SIGN;
    }

    private static boolean isNonFinite(final long value) {
        return (value & MASK_INFINITY_AND_NAN) == MASK_INFINITY_AND_NAN;
    }

    private static long negate(final long value) {
        return value ^ MASK_SIGN;
    }


    private static long toParts(final long value, final Decimal64Parts parts) {
        parts.sign = signBit(value);

        if (isSpecial(value)) {
            if (isNonFinite(value)) {
                parts.exponent = 0;

                parts.coefficient = value & 0xFE03_FFFF_FFFF_FFFFL;
                if (UnsignedLong.compare(value & 0x0003_FFFF_FFFF_FFFFL, MAX_COEFFICIENT) > 0) {
                    parts.coefficient = value & ~MASK_COEFFICIENT;
                }

                if (isInfinity(value)) {
                    parts.coefficient = value & MASK_SIGN_INFINITY_NAN; // TODO: Why this was done??
                }
                return 0;
            }

            // Check for non-canonical values.
            final long coefficient = (value & LARGE_COEFFICIENT_MASK) | LARGE_COEFFICIENT_HIGH_BIT;
            parts.coefficient = UnsignedLong.compare(coefficient, MAX_COEFFICIENT) > 0 ? 0 : coefficient;

            // Extract exponent.
            final long tmp = value >> EXPONENT_SHIFT_LARGE;
            parts.exponent = (int) (tmp & EXPONENT_MASK);

            return parts.coefficient;
        }

        // Extract exponent. Maximum biased value for "small exponent" is 0x2FF(*2=0x5FE), signed: []
        // upper 1/4 of the mask range is "special", as checked in the code above
        final long tmp = value >> EXPONENT_SHIFT_SMALL;
        parts.exponent = (int) (tmp & EXPONENT_MASK);

        // Extract coefficient.
        return parts.coefficient = (value & SMALL_COEFFICIENT_MASK);
    }

    private static long fromParts(final Decimal64Parts parts) {
        return pack(parts.sign, parts.exponent, parts.coefficient, BID_ROUNDING_TO_NEAREST);
    }

    private static class Context {
        public final Decimal64Parts x = new Decimal64Parts();
    }

    private static final ThreadLocal<Context> CONTEXT_MAP = ThreadLocal.withInitial(Context::new);

    private static Appendable appendTo(final long value, final Appendable appendable) throws IOException {
        final Context context = CONTEXT_MAP.get();
        if (toParts(value, context.x) == 0) {
            // Value is ether Inf, or NaN, or 0.
            if (isNaN(value)) {
                return appendable.append("NaN");
            }

            if (!isInfinity(value)) {
                return appendable.append('0');
            }

            return context.x.sign ? appendable.append("-Infinity") : appendable.append("Infinity");
        }

        if (context.x.sign) {
            appendable.append('-');
        }

        final int exponent = context.x.exponent - EXPONENT_BIAS;
        final long coefficient = context.x.coefficient;
        final int digits = numberOfDigits(coefficient);

        if (exponent >= 0) {
            appendLongTo(coefficient, appendable, digits);
            for (int i = 0; i < exponent; i += 1) {
                appendable.append('0');
            }
        } else if (digits + exponent > 0) {
            final long integralPart = coefficient / POWERS_OF_TEN[-exponent];
            final long fractionalPart = coefficient % POWERS_OF_TEN[-exponent];
            appendLongTo(integralPart, appendable);

            if (fractionalPart != 0L) {
                appendable.append('.');

                for (int i = numberOfDigits(fractionalPart); i < -exponent; i += 1) {
                    appendable.append('0');
                }

                appendLongTo(dropTrailingZeros(fractionalPart), appendable);
            }
        } else {
            appendable.append("0.");
            for (int i = digits + exponent; i < 0; i += 1) {
                appendable.append('0');
            }
            appendLongTo(dropTrailingZeros(coefficient), appendable);
        }

        return appendable;
    }

    private static long dropTrailingZeros(long value) {
        assert value >= 0;
        while (value != 0) {
            final long quotient = value / 10;
            final long remainder = value - (quotient << 3) - (quotient << 1);
            if (remainder != 0) {
                return value;
            }
            value = quotient;
        }
        return value;
    }

    private static Appendable appendLongTo(long value, final Appendable appendable) throws IOException {
        return appendLongTo(value, appendable, numberOfDigits(value));
    }

    private static Appendable appendLongTo(long value, final Appendable appendable, final int numberOfDigits) throws IOException {
        assert value >= 0;
        for (int i = numberOfDigits - 1; i >= 1; i -= 1) {
            final long powerOfTen = POWERS_OF_TEN[i];
            final int digit = (int) (value / powerOfTen);
            assert 0 <= digit && digit <= 9;
            appendable.append(DECIMAL_DIGITS[digit]);
            value %= powerOfTen;
        }

        assert 0 <= value && value <= 9;
        return appendable.append(DECIMAL_DIGITS[(int) value]);
    }

    private static final char[] DECIMAL_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private static final long[] POWERS_OF_TEN = {
            /*  0 */ 1L,
            /*  1 */ 10L,
            /*  2 */ 100L,
            /*  3 */ 1000L,
            /*  4 */ 10000L,
            /*  5 */ 100000L,
            /*  6 */ 1000000L,
            /*  7 */ 10000000L,
            /*  8 */ 100000000L,
            /*  9 */ 1000000000L,
            /* 10 */ 10000000000L,
            /* 11 */ 100000000000L,
            /* 12 */ 1000000000000L,
            /* 13 */ 10000000000000L,
            /* 14 */ 100000000000000L,
            /* 15 */ 1000000000000000L,
            /* 16 */ 10000000000000000L,
            /* 17 */ 100000000000000000L,
            /* 18 */ 1000000000000000000L
    };

    /**
     * Computes number of dfp digits required to represent the given non-negative value.
     *
     * @param value Non-negative value.
     * @return Number of digits required.
     */
    private static int numberOfDigits(final long value) {
        for (int i = 1; i < POWERS_OF_TEN.length; i += 1) {
            if (value < POWERS_OF_TEN[i]) {
                return i;
            }
        }

        return 19;
    }

    static final long MASK_SIGN = 0x8000_0000_0000_0000L;
    static final long MASK_SPECIAL = 0x6000_0000_0000_0000L;
    static final long MASK_INFINITY_NAN = 0x7C00_0000_0000_0000L;
    static final long MASK_SIGN_INFINITY_NAN = 0xFC00_0000_0000_0000L; // SIGN|INF|NAN

    static final long MASK_INFINITY_AND_NAN = 0x7800_0000_0000_0000L;

    static final int EXPONENT_BIAS = 398;
    static final int BIASED_EXPONENT_MAX_VALUE = 767;

    static final long LARGE_COEFFICIENT_MASK = 0x0007_FFFF_FFFF_FFFFL;
    static final long LARGE_COEFFICIENT_HIGH_BIT = 0x0020_0000_0000_0000L;

    static final long SMALL_COEFFICIENT_MASK = 0x001F_FFFF_FFFF_FFFFL;
    static final long MASK_COEFFICIENT = 0x0001_FFFF_FFFF_FFFFL;

    static final int EXPONENT_MASK = 0x03FF;
    static final int EXPONENT_SHIFT_LARGE = 51;
    static final int EXPONENT_SHIFT_SMALL = 53;

    private static final int MAX_FORMAT_DIGITS = 16;

    private static NumberFormatException invalidFormat(final CharSequence s, final int si, final int ei) {
        return new NumberFormatException(s.subSequence(si, ei).toString());
    }

    private static long parse(final CharSequence s, final int si, final int ei) {
        char c;
        int p = si;
        boolean sign = false;

        c = s.charAt(p);

        if (c == '+') {
            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        if (c == '-') {
            sign = true;

            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        if (c != '.' && (c < '0' || c > '9')) {
            if (TextUtils.equalsIgnoringCase(s, p, ei, "Infinity") ||
                    TextUtils.equalsIgnoringCase(s, p, ei, "Inf")) {
                return sign ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
            }

            if (TextUtils.equalsIgnoringCase(s, p, ei, "NaN") ||
                    TextUtils.equalsIgnoringCase(s, p, ei, "SNaN")) {
                return NAN;
            }
            throw invalidFormat(s, si, ei);
        }

        boolean seenRadixPoint = false;
        int leadingZerosAfterPoint = 0;

        if (c == '0' || c == '.') {
            if (c == '.') {
                seenRadixPoint = true;

                p += 1;
                c = p < ei ? s.charAt(p) : 0;
            }

            while (c == '0') {
                p += 1;
                c = p < ei ? s.charAt(p) : 0;

                if (seenRadixPoint) {
                    leadingZerosAfterPoint += 1;
                }

                if (c == '.') {
                    if (seenRadixPoint) {
                        throw invalidFormat(s, si, ei);
                    }

                    seenRadixPoint = true;

                    p += 1;
                    c = p < ei ? s.charAt(p) : 0;
                    if (c == 0) {
                        return makeZero(sign, EXPONENT_BIAS - leadingZerosAfterPoint);
                    }
                } else if (c == 0) {
                    return makeZero(sign, EXPONENT_BIAS - leadingZerosAfterPoint);
                }
            }
        }

        int numberOfDigits = 0;
        int decimalExponentScale = 0;
        long coefficient = 0;
        boolean roundedUp = false, rounded = false, midpoint = false;
        int additionalExponent = 0;

        while ((c >= '0' && c <= '9') || c == '.') {
            if (c == '.') {
                if (seenRadixPoint) {
                    throw invalidFormat(s, si, ei);
                }

                seenRadixPoint = true;

                p += 1;
                c = p < ei ? s.charAt(p) : 0;

                continue;
            }

            if (seenRadixPoint) {
                decimalExponentScale += 1;
            }

            numberOfDigits += 1;
            if (numberOfDigits <= 16) {
                coefficient = (coefficient << 1) + (coefficient << 3) + c - '0';
            } else if (numberOfDigits == 17) {
                midpoint = (c == '5' && (coefficient & 1) == 0);
                if (c > '5' || (c == '5' && (coefficient & 1) != 0)) {
                    coefficient += 1;
                    roundedUp = true;
                }

                if (coefficient == 10000000000000000L) {
                    coefficient = 1000000000000000L;
                    additionalExponent = 1;
                }
                if (c > '0') {
                    rounded = true;
                }
                additionalExponent += 1;
            } else {
                additionalExponent += 1;
                if (midpoint && c >= '0') {
                    coefficient += 1;
                    midpoint = false;
                    roundedUp = true;
                }
                if (c >= '0') {
                    rounded = true;
                }
            }

            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        additionalExponent -= (decimalExponentScale + leadingZerosAfterPoint);

        if (c == 0)
            return fastPackCheckOverflow(sign, additionalExponent + EXPONENT_BIAS, coefficient);

        if (c != 'E' && c != 'e')
            throw invalidFormat(s, si, ei);

        p += 1;
        c = p < ei ? s.charAt(p) : 0;

        final boolean isExponentSigned = c == '-';
        if (c == '-' || c == '+') {
            p += 1;
            c = p < ei ? s.charAt(p) : 0;
        }

        if (c == 0 || c < '0' || c > '9') {
            throw invalidFormat(s, si, ei);
        }

        int exponent = 0;
        while (c >= '0' && c <= '9') {
            if (exponent < (1 << 20)) {
                exponent = (exponent << 1) + (exponent << 3) + (c - '0');

                p += 1;
                c = p < ei ? s.charAt(p) : 0;
            }
        }

        if (c != 0)
            throw invalidFormat(s, si, ei);

        if (isExponentSigned)
            exponent = -exponent;
        exponent += additionalExponent + EXPONENT_BIAS;

        if (exponent < 0) {
            if (roundedUp)
                coefficient -= 1;
            return packUnderflow(sign, exponent, coefficient, rounded, BID_ROUNDING_TO_NEAREST);
        }

        return pack(sign, exponent, coefficient, BID_ROUNDING_TO_NEAREST);
    }

    private static long makeZero(final boolean isNegative, final int exponent) {
        final long value = ((long) exponent << EXPONENT_SHIFT_SMALL);
        return isNegative ? negate(value) : value;
    }

    private static long fastPackCheckOverflow(final boolean isSigned, int exponent, long coefficient) {
        long result;
        final long signMask = isSigned ? MASK_SIGN : 0;

        if (UnsignedInteger.compare(exponent, 3 * 256 - 1) >= 0) {
            if ((exponent == 3 * 256 - 1) && coefficient == 10000000000000000L) {
                exponent = 3 * 256;
                coefficient = 1000000000000000L;
            }

            if (UnsignedInteger.compare(exponent, 3 * 256) >= 0) {
                while (coefficient < 1000000000000000L && exponent >= 3 * 256) {
                    exponent--;
                    coefficient = (coefficient << 3) + (coefficient << 1);
                }
                if (exponent > BIASED_EXPONENT_MAX_VALUE)
                    return signMask | POSITIVE_INFINITY;
            }
        }

        long mask = 1L << EXPONENT_SHIFT_SMALL;

        // Check whether coefficient fits in 10 * 5 + 3 bits.
        if (coefficient < mask) {
            result = exponent;
            result <<= EXPONENT_SHIFT_SMALL;
            result |= (coefficient | signMask);
            return result;
        }

        // Eliminate the case coefficient == 10^16 after rounding.
        if (coefficient == 10000000000000000L) {
            result = exponent + 1;
            result <<= EXPONENT_SHIFT_SMALL;
            result |= (1000000000000000L | signMask);
            return result;
        }

        result = exponent;
        result <<= EXPONENT_SHIFT_LARGE;
        result |= (signMask | MASK_SPECIAL);

        // Add coefficient, without leading bits.
        mask = (mask >> 2) - 1;
        coefficient &= mask;
        result |= coefficient;

        return result;
    }

    private final static long UINT32_MAX = 0xFFFF_FFFFL;

    private static long packUnderflow(final boolean isSigned, int exponent, long coefficient,
                                      final boolean rounded, int roundingMode) {
        final long sgn = isSigned ? MASK_SIGN : 0L;
        long C128_0, Q_low_0, Q_low_1;
        long _C64, remainder_h, QH;
        int extra_digits, amount, amount2;

        // Underflow
        if (exponent + MAX_FORMAT_DIGITS < 0) {
            if (roundingMode == BID_ROUNDING_DOWN && isSigned)
                return 0x8000000000000001L;
            if (roundingMode == BID_ROUNDING_UP && !isSigned)
                return 1L;
            return sgn;
        }
        coefficient = (coefficient << 3) + (coefficient << 1);

        if (isSigned && (roundingMode == BID_ROUNDING_DOWN || roundingMode == BID_ROUNDING_UP))
            roundingMode = 3 - roundingMode;

        if (rounded)
            coefficient |= 1;

        // get digits to be shifted out
        extra_digits = 1 - exponent;
        C128_0 = coefficient + bid_round_const_table[roundingMode][extra_digits];

        // get coefficient*(2^M[extra_digits])/10^extra_digits
        {
            long ALBL_0, ALBL_1, ALBH_0, ALBH_1, QM2_0, QM2_1;
            {
                long CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                CXH = (C128_0) >>> 32;
                CXL = C128_0 & UINT32_MAX;
                CYH = (bid_reciprocals10_128[extra_digits][1]) >>> 32;
                CYL = bid_reciprocals10_128[extra_digits][1] & UINT32_MAX;
                PM = CXH * CYL;
                PH = CXH * CYH;
                PL = CXL * CYL;
                PM2 = CXL * CYH;
                PH += (PM >>> 32);
                PM = (PM & UINT32_MAX) + PM2 + (PL >>> 32);
                ALBH_1 = PH + (PM >>> 32);
                ALBH_0 = (PM << 32) + (PL & UINT32_MAX);
            }
            {
                long CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                CXH = ((C128_0)) >>> 32;
                CXL = C128_0 & UINT32_MAX;
                CYH = (bid_reciprocals10_128[extra_digits][0]) >>> 32;
                CYL = bid_reciprocals10_128[extra_digits][0] & UINT32_MAX;
                PM = CXH * CYL;
                PH = CXH * CYH;
                PL = CXL * CYL;
                PM2 = CXL * CYH;
                PH += (PM >>> 32);
                PM = (PM & UINT32_MAX) + PM2 + (PL >>> 32);
                ALBL_1 = PH + (PM >>> 32);
                ALBL_0 = (PM << 32) + (PL & UINT32_MAX);
            }
            Q_low_0 = ALBL_0;
            {
                long R64H;
                R64H = ALBH_1;
                QM2_0 = ALBL_1 + ALBH_0;
                if (QM2_0 < ALBL_1)
                    R64H++;
                QM2_1 = R64H;
            }
            Q_low_1 = QM2_0;
            QH = QM2_1;
        }

        // now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
        amount = bid_recip_scale[extra_digits];

        _C64 = QH >>> amount;

        if (roundingMode == BID_ROUNDING_TO_NEAREST)
            if ((_C64 & 1) != 0) {
                // check whether fractional part of initial_P/10^extra_digits is exactly .5

                // get remainder
                amount2 = 64 - amount;
                remainder_h = 0;
                remainder_h--;
                remainder_h >>>= amount2;
                remainder_h = remainder_h & QH;

                if (remainder_h == 0
                        && (Q_low_1 < bid_reciprocals10_128[extra_digits][1]
                        || (Q_low_1 == bid_reciprocals10_128[extra_digits][1]
                        && Q_low_0 < bid_reciprocals10_128[extra_digits][0]))) {
                    _C64--;
                }
            }

        return sgn | _C64;
    }

    private static final int[] bid_recip_scale = {
            129 - 128,    // 1
            129 - 128,    // 1/10
            129 - 128,    // 1/10^2
            129 - 128,    // 1/10^3
            3,    // 131 - 128
            6,    // 134 - 128
            9,    // 137 - 128
            13,    // 141 - 128
            16,    // 144 - 128
            19,    // 147 - 128
            23,    // 151 - 128
            26,    // 154 - 128
            29,    // 157 - 128
            33,    // 161 - 128
            36,    // 164 - 128
            39,    // 167 - 128
            43,    // 171 - 128
            46,    // 174 - 128
            49,    // 177 - 128
            53,    // 181 - 128
            56,    // 184 - 128
            59,    // 187 - 128
            63,    // 191 - 128

            66,    // 194 - 128
            69,    // 197 - 128
            73,    // 201 - 128
            76,    // 204 - 128
            79,    // 207 - 128
            83,    // 211 - 128
            86,    // 214 - 128
            89,    // 217 - 128
            92,    // 220 - 128
            96,    // 224 - 128
            99,    // 227 - 128
            102,    // 230 - 128
            109,    // 237 - 128, 1/10^35
    };

    private static final long[][] bid_round_const_table = new long[][]{
            {    // RN
                    0L,    // 0 extra digits
                    5L,    // 1 extra digits
                    50L,    // 2 extra digits
                    500L,    // 3 extra digits
                    5000L,    // 4 extra digits
                    50000L,    // 5 extra digits
                    500000L,    // 6 extra digits
                    5000000L,    // 7 extra digits
                    50000000L,    // 8 extra digits
                    500000000L,    // 9 extra digits
                    5000000000L,    // 10 extra digits
                    50000000000L,    // 11 extra digits
                    500000000000L,    // 12 extra digits
                    5000000000000L,    // 13 extra digits
                    50000000000000L,    // 14 extra digits
                    500000000000000L,    // 15 extra digits
                    5000000000000000L,    // 16 extra digits
                    50000000000000000L,    // 17 extra digits
                    500000000000000000L    // 18 extra digits
            },
            {    // RD
                    0L,    // 0 extra digits
                    0L,    // 1 extra digits
                    0L,    // 2 extra digits
                    0L,    // 3 extra digits
                    0L,    // 4 extra digits
                    0L,    // 5 extra digits
                    0L,    // 6 extra digits
                    0L,    // 7 extra digits
                    0L,    // 8 extra digits
                    0L,    // 9 extra digits
                    0L,    // 10 extra digits
                    0L,    // 11 extra digits
                    0L,    // 12 extra digits
                    0L,    // 13 extra digits
                    0L,    // 14 extra digits
                    0L,    // 15 extra digits
                    0L,    // 16 extra digits
                    0L,    // 17 extra digits
                    0L    // 18 extra digits
            },
            {    // round to Inf
                    0L,    // 0 extra digits
                    9L,    // 1 extra digits
                    99L,    // 2 extra digits
                    999L,    // 3 extra digits
                    9999L,    // 4 extra digits
                    99999L,    // 5 extra digits
                    999999L,    // 6 extra digits
                    9999999L,    // 7 extra digits
                    99999999L,    // 8 extra digits
                    999999999L,    // 9 extra digits
                    9999999999L,    // 10 extra digits
                    99999999999L,    // 11 extra digits
                    999999999999L,    // 12 extra digits
                    9999999999999L,    // 13 extra digits
                    99999999999999L,    // 14 extra digits
                    999999999999999L,    // 15 extra digits
                    9999999999999999L,    // 16 extra digits
                    99999999999999999L,    // 17 extra digits
                    999999999999999999L    // 18 extra digits
            },
            {    // RZ
                    0L,    // 0 extra digits
                    0L,    // 1 extra digits
                    0L,    // 2 extra digits
                    0L,    // 3 extra digits
                    0L,    // 4 extra digits
                    0L,    // 5 extra digits
                    0L,    // 6 extra digits
                    0L,    // 7 extra digits
                    0L,    // 8 extra digits
                    0L,    // 9 extra digits
                    0L,    // 10 extra digits
                    0L,    // 11 extra digits
                    0L,    // 12 extra digits
                    0L,    // 13 extra digits
                    0L,    // 14 extra digits
                    0L,    // 15 extra digits
                    0L,    // 16 extra digits
                    0L,    // 17 extra digits
                    0L    // 18 extra digits
            },
            {    // round ties away from 0
                    0L,    // 0 extra digits
                    5L,    // 1 extra digits
                    50L,    // 2 extra digits
                    500L,    // 3 extra digits
                    5000L,    // 4 extra digits
                    50000L,    // 5 extra digits
                    500000L,    // 6 extra digits
                    5000000L,    // 7 extra digits
                    50000000L,    // 8 extra digits
                    500000000L,    // 9 extra digits
                    5000000000L,    // 10 extra digits
                    50000000000L,    // 11 extra digits
                    500000000000L,    // 12 extra digits
                    5000000000000L,    // 13 extra digits
                    50000000000000L,    // 14 extra digits
                    500000000000000L,    // 15 extra digits
                    5000000000000000L,    // 16 extra digits
                    50000000000000000L,    // 17 extra digits
                    500000000000000000L    // 18 extra digits
            }
    };


    private static final long[][] bid_reciprocals10_128 = {
            {0L, 0L},                                      // 0 extra digits
            {0x3333333333333334L, 0x3333333333333333L},    // 1 extra digit
            {0x51eb851eb851eb86L, 0x051eb851eb851eb8L},    // 2 extra digits
            {0x3b645a1cac083127L, 0x0083126e978d4fdfL},    // 3 extra digits
            {0x4af4f0d844d013aaL, 0x00346dc5d6388659L},    //  10^(-4) * 2^131
            {0x08c3f3e0370cdc88L, 0x0029f16b11c6d1e1L},    //  10^(-5) * 2^134
            {0x6d698fe69270b06dL, 0x00218def416bdb1aL},    //  10^(-6) * 2^137
            {0xaf0f4ca41d811a47L, 0x0035afe535795e90L},    //  10^(-7) * 2^141
            {0xbf3f70834acdaea0L, 0x002af31dc4611873L},    //  10^(-8) * 2^144
            {0x65cc5a02a23e254dL, 0x00225c17d04dad29L},    //  10^(-9) * 2^147
            {0x6fad5cd10396a214L, 0x0036f9bfb3af7b75L},    // 10^(-10) * 2^151
            {0xbfbde3da69454e76L, 0x002bfaffc2f2c92aL},    // 10^(-11) * 2^154
            {0x32fe4fe1edd10b92L, 0x00232f33025bd422L},    // 10^(-12) * 2^157
            {0x84ca19697c81ac1cL, 0x00384b84d092ed03L},    // 10^(-13) * 2^161
            {0x03d4e1213067bce4L, 0x002d09370d425736L},    // 10^(-14) * 2^164
            {0x3643e74dc052fd83L, 0x0024075f3dceac2bL},    // 10^(-15) * 2^167
            {0x56d30baf9a1e626bL, 0x0039a5652fb11378L},    // 10^(-16) * 2^171
            {0x12426fbfae7eb522L, 0x002e1dea8c8da92dL},    // 10^(-17) * 2^174
            {0x41cebfcc8b9890e8L, 0x0024e4bba3a48757L},    // 10^(-18) * 2^177
            {0x694acc7a78f41b0dL, 0x003b07929f6da558L},    // 10^(-19) * 2^181
            {0xbaa23d2ec729af3eL, 0x002f394219248446L},    // 10^(-20) * 2^184
            {0xfbb4fdbf05baf298L, 0x0025c768141d369eL},    // 10^(-21) * 2^187
            {0x2c54c931a2c4b759L, 0x003c7240202ebdcbL},    // 10^(-22) * 2^191
            {0x89dd6dc14f03c5e1L, 0x00305b66802564a2L},    // 10^(-23) * 2^194
            {0xd4b1249aa59c9e4eL, 0x0026af8533511d4eL},    // 10^(-24) * 2^197
            {0x544ea0f76f60fd49L, 0x003de5a1ebb4fbb1L},    // 10^(-25) * 2^201
            {0x76a54d92bf80caa1L, 0x00318481895d9627L},    // 10^(-26) * 2^204
            {0x921dd7a89933d54eL, 0x00279d346de4781fL},    // 10^(-27) * 2^207
            {0x8362f2a75b862215L, 0x003f61ed7ca0c032L},    // 10^(-28) * 2^211
            {0xcf825bb91604e811L, 0x0032b4bdfd4d668eL},    // 10^(-29) * 2^214
            {0x0c684960de6a5341L, 0x00289097fdd7853fL},    // 10^(-30) * 2^217
            {0x3d203ab3e521dc34L, 0x002073accb12d0ffL},    // 10^(-31) * 2^220
            {0x2e99f7863b696053L, 0x0033ec47ab514e65L},    // 10^(-32) * 2^224
            {0x587b2c6b62bab376L, 0x002989d2ef743eb7L},    // 10^(-33) * 2^227
            {0xad2f56bc4efbc2c5L, 0x00213b0f25f69892L},    // 10^(-34) * 2^230
            {0x0f2abc9d8c9689d1L, 0x01a95a5b7f87a0efL},    // 35 extra digits
    };

    private static final int BID_ROUNDING_TO_NEAREST = 0x00000;
    private static final int BID_ROUNDING_DOWN = 0x00001;
    private static final int BID_ROUNDING_UP = 0x00002;
    private static final int BID_ROUNDING_TO_ZERO = 0x00003;
    private static final int BID_ROUNDING_TIES_AWAY = 0x00004;

    private static long pack(final boolean isSigned, int exponent, long coefficient, int roundingMode) {
        final long sgn = isSigned ? MASK_SIGN : 0L;

        long Q_low_0, Q_low_1;
        long QH, r, mask, _C64, remainder_h;

        int extra_digits, amount, amount2;

        if (UnsignedLong.compare(coefficient, 9999999999999999L) > 0) {
            exponent++;
            coefficient = 1000000000000000L;
        }

        // Check for possible underflow/overflow.
        if (exponent > BIASED_EXPONENT_MAX_VALUE || exponent < 0) {
            if (exponent < 0) {
                // Underflow.
                if (exponent + MAX_FORMAT_DIGITS < 0) {
                    if (roundingMode == BID_ROUNDING_DOWN && isSigned)
                        return 0x8000000000000001L;
                    if (roundingMode == BID_ROUNDING_UP && !isSigned)
                        return 1L;
                    return sgn;
                }

                if (isSigned && (roundingMode == BID_ROUNDING_DOWN || roundingMode == BID_ROUNDING_UP))
                    roundingMode = 3 - roundingMode;

                // Get digits to be shifted out
                extra_digits = -exponent;
                coefficient += bid_round_const_table[roundingMode][extra_digits];

                // Get coefficient * (2^M[extra_digits])/10^extra_digits
                {
                    long ALBL_0, ALBL_1, ALBH_0, ALBH_1, QM2_0, QM2_1;
                    {
                        long CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                        CXH = coefficient >>> 32;
                        CXL = (int) ((coefficient));
                        CYH = bid_reciprocals10_128[extra_digits][1] >>> 32;
                        CYL = (int) bid_reciprocals10_128[extra_digits][1];
                        PM = CXH * CYL;
                        PH = CXH * CYH;
                        PL = CXL * CYL;
                        PM2 = CXL * CYH;
                        PH += (PM >>> 32);
                        PM = (PM & 0xFFFFFFFFL) + PM2 + (PL >> 32);
                        ALBH_1 = PH + (PM >> 32);
                        ALBH_0 = (PM << 32) + (int) PL;
                    }
                    {
                        long CXH, CXL, CYH, CYL, PL, PH, PM, PM2;
                        CXH = ((coefficient)) >>> 32;
                        CXL = (int) ((coefficient));
                        CYH = bid_reciprocals10_128[extra_digits][0] >>> 32;
                        CYL = (int) bid_reciprocals10_128[extra_digits][0];
                        PM = CXH * CYL;
                        PH = CXH * CYH;
                        PL = CXL * CYL;
                        PM2 = CXL * CYH;
                        PH += (PM >>> 32);
                        PM = (PM & 0xFFFFFFFFL) + PM2 + (PL >>> 32);
                        ALBL_1 = PH + (PM >>> 32);
                        ALBL_0 = (PM << 32) + (PL & 0xFFFFFFFFL);
                    }
                    Q_low_0 = ALBL_0;
                    {
                        long R64H;
                        R64H = ALBH_1;
                        QM2_0 = ALBL_1 + ALBH_0;
                        if (QM2_0 < ALBL_1)
                            R64H++;
                        QM2_1 = R64H;
                    }
                    Q_low_1 = QM2_0;
                    QH = QM2_1;
                }

                // Now get P/10^extra_digits: shift Q_high right by M[extra_digits]-128
                amount = bid_recip_scale[extra_digits];

                _C64 = QH >>> amount;

                if (roundingMode == BID_ROUNDING_TO_NEAREST)
                    if ((_C64 & 1) != 0) {
                        // Check whether fractional part of initial_P/10^extra_digits is exactly .5
                        // Get remainder
                        amount2 = 64 - amount;
                        remainder_h = 0;
                        remainder_h--;
                        remainder_h >>>= amount2;
                        remainder_h = remainder_h & QH;

                        if (remainder_h == 0L
                                && (Q_low_1 < bid_reciprocals10_128[extra_digits][1]
                                || (Q_low_1 == bid_reciprocals10_128[extra_digits][1]
                                && Q_low_0 < bid_reciprocals10_128[extra_digits][0]))) {
                            _C64--;
                        }
                    }
                return sgn | _C64;
            }
            if (coefficient == 0L) {
                if (exponent > BIASED_EXPONENT_MAX_VALUE) {
                    exponent = BIASED_EXPONENT_MAX_VALUE;
                }
            }
            while (UnsignedLong.compare(coefficient, 1000000000000000L) < 0 && exponent >= 3 * 256) {
                exponent--;
                coefficient = (coefficient << 3) + (coefficient << 1);
            }
            if (exponent > BIASED_EXPONENT_MAX_VALUE) {
                // Overflow
                r = sgn | MASK_INFINITY_AND_NAN;
                switch (roundingMode) {
                    case BID_ROUNDING_DOWN:
                        if (!isSigned) {
                            r = MAX_VALUE;
                        }
                        break;

                    case BID_ROUNDING_TO_ZERO:
                        r = sgn | MAX_VALUE;
                        break;

                    case BID_ROUNDING_UP:
                        if (isSigned) {
                            r = MIN_VALUE;
                        }
                }
                return r;
            }
        }

        mask = 1;
        mask <<= EXPONENT_SHIFT_SMALL;

        // Check whether coefficient fits in 10 * 5 + 3 bits.
        if (coefficient < mask) {
            r = exponent;
            r <<= EXPONENT_SHIFT_SMALL;
            r |= (coefficient | sgn);
            return r;
        }
        // Special format.

        // Eliminate the case coefficient == 10^16 after rounding.
        if (UnsignedLong.compare(coefficient, 10000000000000000L) == 0) {
            r = exponent + 1L;
            r <<= EXPONENT_SHIFT_SMALL;
            r |= (1000000000000000L | sgn);
            return r;
        }

        r = exponent;
        r <<= EXPONENT_SHIFT_LARGE;
        r |= (sgn | MASK_SPECIAL);

        // Add coefficient, without leading bits.
        mask = (mask >>> 2) - 1;
        coefficient &= mask;
        r |= coefficient;

        return r;
    }


    private static class TextUtils {
        public static boolean equalsIgnoringCase(final CharSequence s1, final int s1si, final int s1ei,
                                                 final CharSequence s2) {
            if (s1ei < s1si) {
                throw new IllegalArgumentException("End index is expected to be greater or equal to start index.");
            }

            if (s2.length() != s1ei - s1si) {
                return false;
            }

            for (int i2 = 0, i1 = s1si; i2 < s1.length() && i1 < s1ei; i2 += 1, i1 += 1) {
                if (Character.toLowerCase(s1.charAt(i1)) != Character.toLowerCase(s2.charAt(i2))) {
                    return false;
                }
            }

            return true;
        }
    }

    private static class Decimal64Parts {
        long coefficient;
        int exponent;
        boolean sign;

        public Decimal64Parts() {
            this(0L, 0, false);
        }

        public Decimal64Parts(final long coefficient, final int exponent, final boolean sign) {
            this.coefficient = coefficient;
            this.exponent = exponent;
            this.sign = sign;
        }

        public long getCoefficient() {
            return coefficient;
        }

        public void setCoefficient(final long coefficient) {
            this.coefficient = coefficient;
        }

        public int getExponent() {
            return exponent;
        }

        public void setExponent(final int exponent) {
            this.exponent = exponent;
        }

        public boolean isNegative() {
            return sign;
        }

        public void setNegative(final boolean negative) {
            sign = negative;
        }
    }

    private static class UnsignedLong {
        public static int compare(final long x, final long y) {
            return Long.compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
        }

        public static long parse(String s, int radix)
                throws NumberFormatException {
            if (s == null) {
                throw new NumberFormatException("null");
            }

            int len = s.length();
            if (len > 0) {
                char firstChar = s.charAt(0);
                if (firstChar == '-') {
                    throw new
                            NumberFormatException(String.format("Illegal leading minus sign " +
                            "on unsigned string %s.", s));
                } else {
                    if (len <= 12 || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
                            (radix == 10 && len <= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
                        return Long.parseLong(s, radix);
                    }

                    // No need for range checks on len due to testing above.
                    long first = Long.parseLong(s.substring(0, len - 1), radix);
                    int second = Character.digit(s.charAt(len - 1), radix);
                    if (second < 0) {
                        throw new NumberFormatException("Bad digit at end of " + s);
                    }
                    long result = first * radix + second;
                    if (compare(result, first) < 0) {
                        /*
                         * The maximum unsigned value, (2^64)-1, takes at
                         * most one more digit to represent than the
                         * maximum signed value, (2^63)-1.  Therefore,
                         * parsing (len - 1) digits will be appropriately
                         * in-range of the signed parsing.  In other
                         * words, if parsing (len -1) digits overflows
                         * signed parsing, parsing len digits will
                         * certainly overflow unsigned parsing.
                         *
                         * The compareUnsigned check above catches
                         * situations where an unsigned overflow occurs
                         * incorporating the contribution of the final
                         * digit.
                         */
                        throw new NumberFormatException(String.format("String value %s exceeds " +
                                "range of unsigned long.", s));
                    }
                    return result;
                }
            } else {
                throw new NumberFormatException("For input string: \"" + s + "\"");
            }
        }
    }

    private static class UnsignedInteger {
        public static int compare(final int x, final int y) {
            return Integer.compare(x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
        }
    }

}
