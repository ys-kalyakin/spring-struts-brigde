package org.apache.struts.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the beanutils-style parameter names accepted by populate: a
 * dot-separated path of segments, each segment being a property name with
 * an optional index (<code>prop[0]</code>) and an optional mapped key
 * (<code>prop(key)</code>), including combinations
 * (<code>a.b[0](key)</code>). Dots inside parentheses or brackets do not
 * split segments, so mapped keys and index expressions may contain them.
 */
final class StrutsExpressionParser {

    /**
     * One resolved segment of a parameter name: the property name, the
     * optional index and the optional mapped key.
     */
    static final class Segment {

        private final String name;
        private final Integer index;
        private final String key;

        Segment(String name, Integer index, String key) {
            this.name = name;
            this.index = index;
            this.key = key;
        }

        String getName() {
            return name;
        }

        Integer getIndex() {
            return index;
        }

        String getKey() {
            return key;
        }

        boolean isMapped() {
            return key != null;
        }

        boolean isIndexed() {
            return index != null;
        }
    }

    private StrutsExpressionParser() {
    }

    /**
     * Parse a parameter name into its segments; an unparseable name yields
     * an empty list so the caller skips it.
     */
    static List<Segment> parse(String name) {
        List<Segment> segments = new ArrayList<>();
        for (String raw : splitSegments(name)) {
            Segment segment = parseSegment(raw);
            if (segment == null) {
                return List.of();
            }
            segments.add(segment);
        }
        return segments;
    }

    /**
     * Split at dots outside of parentheses and brackets.
     */
    private static List<String> splitSegments(String name) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            switch (ch) {
            case '(':
            case '[':
                depth++;
                break;
            case ')':
            case ']':
                depth--;
                break;
            case '.':
                if (depth == 0) {
                    parts.add(name.substring(start, i));
                    start = i + 1;
                }
                break;
            default:
                break;
            }
        }
        parts.add(name.substring(start));
        return parts;
    }

    /**
     * Parse one segment: <code>name</code>, optionally followed by
     * <code>[index]</code> and <code>(key)</code> in any order.
     */
    private static Segment parseSegment(String raw) {
        String name = raw;
        Integer index = null;
        String key = null;

        int open;
        while ((open = indexOfAny(name, '[', '(')) >= 0) {
            char openChar = name.charAt(open);
            char closeChar = (openChar == '[') ? ']' : ')';
            int close = name.indexOf(closeChar, open + 1);
            if (close < 0) {
                return null;
            }
            String inner = name.substring(open + 1, close);
            if (openChar == '[') {
                index = parseIndex(inner);
                if (index == null) {
                    return null;
                }
            } else {
                key = inner;
            }
            name = name.substring(0, open) + name.substring(close + 1);
        }

        if (name.isEmpty()) {
            return null;
        }
        return new Segment(name, index, key);
    }

    private static int indexOfAny(String value, char first, char second) {
        int firstIndex = value.indexOf(first);
        int secondIndex = value.indexOf(second);
        if (firstIndex < 0) {
            return secondIndex;
        }
        if (secondIndex < 0) {
            return firstIndex;
        }
        return Math.min(firstIndex, secondIndex);
    }

    private static Integer parseIndex(String inner) {
        try {
            return Integer.valueOf(Integer.parseInt(inner.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
