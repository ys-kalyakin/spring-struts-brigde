package org.apache.struts.validator.validwhen;

/**
 * Holds the (interim) result during processing the parsing-tree.
 * The result has is a {@code String}.
 *
 * @author Graff Stefan
 *
 * @since Struts 1.4.1
 */
public class ValidWhenResultString extends ValidWhenResult<String> {

    /**
     * Constructs this class with the type {@code String}.
     *
     * @param value the current value of
     *     the result
     */
    public ValidWhenResultString(String value) {
        super(String.class, initValue(value));
    }

    /**
     * Trims the {@code value} and returns {@code null}
     * if the {@code value} is empty.
     *
     * @param value the value
     *
     * @return the trimmed {@code String} or {@code null}
     */
    private static String initValue(String value) {
        if (value == null) {
            return null;
        }

        String s = value.trim();
        return s.isEmpty() ? null : s;
    }
}
