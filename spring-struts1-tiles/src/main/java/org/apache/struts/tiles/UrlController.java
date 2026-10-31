package org.apache.struts.tiles;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tiles controller including a local URL.
 */
public class UrlController implements Controller {
    private static final long serialVersionUID = -3427605980038213048L;

    /**
     * URL associated with this controller.
     */
    protected String url = null;

    /**
     * Constructor.
     * @param url URL.
     */
    public UrlController(String url) {
        this.url = url;
    }

    /**
     * Method associated to a tile and called immediately before the tile
     * is included.  This implementation calls an <code>Action</code>.
     * No servlet is set by this method.
     *
     * @param tileContext Current tile context.
     * @param request Current request.
     * @param response Current response.
     * @param servletContext Current servlet context.
     * @deprecated Use execute() instead.  This will be removed after
     *             Struts 1.2.
     */
    @Deprecated
    public void perform(
        ComponentContext tileContext,
        HttpServletRequest request,
        HttpServletResponse response,
        ServletContext servletContext)
        throws ServletException, IOException {

        RequestDispatcher rd = servletContext.getRequestDispatcher(url);
        if (rd == null) {
            throw new ServletException(
                "Controller can't find url '" + url + "'.");
        }

        rd.include(request, response);
    }

    /**
     * @see Controller#execute(ComponentContext, HttpServletRequest,
     * HttpServletResponse, ServletContext)
     */
    public void execute(
        ComponentContext tileContext,
        HttpServletRequest request,
        HttpServletResponse response,
        ServletContext servletContext)
        throws Exception {

        RequestDispatcher rd = servletContext.getRequestDispatcher(url);
        if (rd == null) {
            throw new ServletException(
                "Controller can't find url '" + url + "'.");
        }

        rd.include(request, response);
    }
}
