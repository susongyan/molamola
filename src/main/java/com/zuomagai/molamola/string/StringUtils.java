package com.zuomagai.molamola.string;

import java.util.Iterator;

public final class StringUtils {

    private StringUtils() {
        throw new AssertionError("No instances.");
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String defaultIfNull(String str, String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    public static boolean equals(String a, String b) {
        return a == b || (a != null && a.equals(b));
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        return a == b || (a != null && b != null && a.equalsIgnoreCase(b));
    }

    public static boolean contains(String str, String search) {
        return str != null && search != null && str.contains(search);
    }

    public static boolean containsIgnoreCase(String str, String search) {
        if (str == null || search == null) {
            return false;
        }
        int searchLen = search.length();
        if (searchLen == 0) {
            return true;
        }
        int max = str.length() - searchLen;
        for (int i = 0; i <= max; i++) {
            if (str.regionMatches(true, i, search, 0, searchLen)) {
                return true;
            }
        }
        return false;
    }

    public static String join(Object[] items, String delimiter) {
        if (items == null || items.length == 0) {
            return "";
        }
        String realDelimiter = delimiter == null ? "" : delimiter;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                builder.append(realDelimiter);
            }
            if (items[i] != null) {
                builder.append(items[i]);
            }
        }
        return builder.toString();
    }

    public static String join(Iterable<?> items, String delimiter) {
        if (items == null) {
            return "";
        }
        String realDelimiter = delimiter == null ? "" : delimiter;
        StringBuilder builder = new StringBuilder();
        Iterator<?> iterator = items.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                builder.append(realDelimiter);
            }
            Object item = iterator.next();
            if (item != null) {
                builder.append(item);
            }
            first = false;
        }
        return builder.toString();
    }

    public static String repeat(String str, int count) {
        if (str == null) {
            return null;
        }
        if (count <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(str);
        }
        return builder.toString();
    }
}
