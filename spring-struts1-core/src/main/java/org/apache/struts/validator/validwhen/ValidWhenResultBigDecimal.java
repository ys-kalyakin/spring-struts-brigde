package org.apache.struts.validator.validwhen;

/**
 * Holds the (interim) result during processing the parsing-tree.
 * The result has is a {@code BigDecimal}.
 *
 * @author Graff Stefan
 *
 * @since Struts 1.4.1
 */
import java.math.BigDecimal;

/**
 * Holds the (interim) result during processing the parsing-tree.
 *
 * @author Graff Stefan
 */
public class ValidWhenResultBigDecimal extends ValidWhenResult<BigDecimal> {

    /**
     * Constructs this class with the type {@code BigDecimal}.
     *
     * @param value the current value of
     *     the result
     */
    public ValidWhenResultBigDecimal(String value) {
        super(BigDecimal.class, new BigDecimal(value));
    }
}
