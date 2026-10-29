package org.apache.struts.tiles.taglib;

/**
 * This is the tag handler for &lt;tiles:get&gt;, which gets
 * content from the request scope and either includes the content or prints
 * it, depending upon the value of the content's <code>direct</code> attribute.
 *
 * This tag is intended to be compatible with the same tag from Templates (David Geary).
 * Implementation extends InsertTag for facility (no so well).
 * The only difference is the default value of attribute 'ignore', which is <code>true</code>
 * for this tag (default behavior of David Geary's templates).
 */
public class GetTag extends InsertTag {
    private static final long serialVersionUID = 8110886541599103180L;


    /**
     * Constructor.
     * Set default value for 'isErrorIgnored' to <code>true</code>.
     */
    public GetTag() {
        isErrorIgnored = true;
    }

    /**
     * Release all allocated resources.
     */
    public void release() {

        super.release();
        isErrorIgnored = true;
    }


}
