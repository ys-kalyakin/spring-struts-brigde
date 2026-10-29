package org.apache.struts.validator.validwhen;

/**
 * Holds the (interim) result during processing the parsing-tree.
 * The result has is a {@code boolean}.
 *
 * @author Graff Stefan
 *
 * @since Struts 1.4.1
 */
public class ValidWhenResultBoolean extends ValidWhenResult<Boolean> {

    /**
     * Constructs this class with the type {@code boolean}.
     *
     * @param value the current value of
     *     the result
     */
    public ValidWhenResultBoolean(boolean value) {
        super(boolean.class, value);
    }

    @Override
    public boolean toBoolean() {
        return value;
    }
}
