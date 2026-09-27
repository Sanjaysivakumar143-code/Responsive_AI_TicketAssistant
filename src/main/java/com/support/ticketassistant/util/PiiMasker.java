package com.support.ticketassistant.util;

import java.util.regex.Pattern;

public final class PiiMasker {

    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");
    private static final Pattern CARD_NUMBER = Pattern.compile("\\b(?:\\d[ -]?){13,19}\\b");
    private static final Pattern PHONE_NUMBER = Pattern.compile("\\b\\+?\\d[\\d -]{7,}\\d\\b");

    private PiiMasker() {}

    public static String mask(String input) {
        if (input == null) return null;
        String masked = EMAIL.matcher(input).replaceAll("[REDACTED_EMAIL]");
        masked = CARD_NUMBER.matcher(masked).replaceAll("[REDACTED_CARD_NUMBER]");
        masked = PHONE_NUMBER.matcher(masked).replaceAll("[REDACTED_PHONE]");
        return masked;
    }
}
