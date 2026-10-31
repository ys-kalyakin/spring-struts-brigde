package org.apache.struts.tiles;

import java.io.IOException;
import java.io.Serializable;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A controller is a piece of code called before rendering a jsp page.
 * A controller can be associated to a tile. See &lt;insert&gt; or
 * &lt;definition&gt; for association syntax.
 */
public interface Controller extends Serializable {

    /**
     * Method associated to a tile and called immediately before the tile
     * is included.
     *
     * @param tileContext    Current tile context.
     * @param request        Current request
     * @param response       Current response
     * @param servletContext Current servlet context
     * @deprecated Use execute() instead.  This will be removed after
     *             Struts 1.2.
     */
    @Deprecated
    void perform(
            ComponentContext tileContext,
            HttpServletRequest request,
            HttpServletResponse response,
            ServletContext servletContext)
            throws ServletException, IOException;

    /**
     * Method associated to a tile and called immediately before the tile
     * is included.
     *
     * @param tileContext    Current tile context.
     * @param request        Current request
     * @param response       Current response
     * @param servletContext Current servlet context
     */
    void execute(
            ComponentContext tileContext,
            HttpServletRequest request,
            HttpServletResponse response,
            ServletContext servletContext)
            throws Exception;
}
