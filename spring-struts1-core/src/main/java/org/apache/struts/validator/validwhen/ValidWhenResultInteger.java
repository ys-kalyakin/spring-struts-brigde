package org.apache.struts.validator.validwhen;

/**
 * Holds the (interim) result during processing the parsing-tree.
 * The result has is a {@code int}.
 *
 * @author Graff Stefan
 *
 * @since Struts 1.4.1
 */
public class ValidWhenResultInteger extends ValidWhenResult<Integer> {

    /**
     * Constructs this class with the type {@code int}.
     *
     * @param value the current value of
     *     the result
     */
    public ValidWhenResultInteger(String value) {
        super(int.class, Integer.decode(value));
    }
}
