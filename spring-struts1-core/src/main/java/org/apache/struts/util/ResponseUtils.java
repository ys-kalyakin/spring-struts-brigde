package org.apache.struts.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General purpose utility methods related to generating a servlet response in
 * the Struts controller framework.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.ResponseUtils).</p>
 */
public class ResponseUtils {

    private static final Logger LOG =
        LoggerFactory.getLogger(ResponseUtils.class);

    /**
     * Pattern to find XML-Entity-Patterns.
     *
     * <p>Valid patterns are:</p>
     *
     * <ul>
     * <li>&amp;[a-zA-Z][a-z-A-Z0-9]*; - &amp;amp;, &amp;quot;</li>
     * <li>&amp;#[0-9]+; - &amp;#32;, &amp;#64;</li>
     * <li>&amp;#x[a-fA-F0-9]+; - &amp;#x20, &amp;3f</li>
     * </ul>
     */
    private static final Pattern XML_ENTITY_PATTERN =
        Pattern.compile("&(?:[a-zA-Z][a-zA-Z\\d]*|#\\d+|#x[a-fA-F\\d]+);");

    /**
     * Class is not instanceable.
     */
    protected ResponseUtils() {
    }

    /**
     * Filter the specified string for characters that are sensitive to HTML
     * interpreters, returning the string with these characters replaced by
     * the corresponding character entities. Existing XML entities in the
     * input are passed through unchanged.
     */
    public static String filter(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        final int length = value.length();

        StringBuilder result = null;
        String filtered;
        Matcher entityMatcher = null;

        for (int i = 0; i < length; i++) {
            filtered = null;

            switch (value.charAt(i)) {
                case '<':
                    filtered = "&lt;";

                    break;

                case '>':
                    filtered = "&gt;";

                    break;

                case '&':
                    if (entityMatcher == null) {
                        entityMatcher = XML_ENTITY_PATTERN.matcher(value);
                    }
                    entityMatcher.region(i, length);
                    if (entityMatcher.lookingAt()) {
                        filtered = value.substring(entityMatcher.start(),
                        entityMatcher.end());
                        i += filtered.length() - 1;
                        if (result == null) {
                            continue;
                        }
                    } else {
                        filtered = "&amp;";
                    }

                    break;

                case '"':
                    filtered = "&quot;";

                    break;

                case '\'':
                    filtered = "&#39;";

                    break;

                default:
                    break;
            }

            if (result == null) {
                if (filtered != null) {
                    result = new StringBuilder(length + 50);

                    if (i > 0) {
                        result.append(value.substring(0, i));
                    }

                    result.append(filtered);
                }
            } else {
                if (filtered == null) {
                    result.append(value.charAt(i));
                } else {
                    result.append(filtered);
                }
            }
        }

        return result == null ? value : result.toString();
    }

    /**
     * URLencodes a string assuming the character encoding is UTF-8.
     */
    public static String encodeURL(String url) {
        return encodeURL(url, "UTF-8");
    }

    /**
     * Use the URLEncoder.encode() with the given encoding. When the encoding
     * charset is not found, it will be tried with the default charset.
     */
    public static String encodeURL(String url, String enc) {
        String str = null;

        try {
            if (enc == null || enc.isEmpty()) {
                enc = "UTF-8";
            }

            str = URLEncoder.encode(url, enc);
        } catch (UnsupportedEncodingException e) {
            LOG.debug("The named encoding is not supported: {}", enc, e);

            try {
                str = URLEncoder.encode(url,
                    Charset.defaultCharset().toString());
            } catch (UnsupportedEncodingException ex) {
                LOG.debug("The default-encoding is not supported: {}", enc, ex);

                str = url;
            }
        }

        return str;
    }
}
