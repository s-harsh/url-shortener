package com.urlshortener.util;

public final class Base62Encoder {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length(); // 62

    private Base62Encoder() {}

    public static String encode(long number) {
        if (number < 0) throw new IllegalArgumentException("Number must be non-negative");
        if (number == 0) return String.valueOf(ALPHABET.charAt(0));

        StringBuilder sb = new StringBuilder();
        long n = number;
        while (n > 0) {
            sb.insert(0, ALPHABET.charAt((int) (n % BASE)));
            n /= BASE;
        }
        return sb.toString();
    }

    public static long decode(String encoded) {
        long result = 0;
        for (char c : encoded.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) throw new IllegalArgumentException("Invalid Base62 character: " + c);
            result = result * BASE + index;
        }
        return result;
    }

    // Left-pad with '0' to targetLength
    public static String encodeWithPadding(long number, int targetLength) {
        String encoded = encode(number);
        if (encoded.length() >= targetLength) return encoded;
        return "0".repeat(targetLength - encoded.length()) + encoded;
    }
}
