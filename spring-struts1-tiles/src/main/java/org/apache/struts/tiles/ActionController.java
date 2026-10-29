package org.apache.struts.tiles;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;

/**
 * Struts wrapper implementation of Controller.  This implementation wraps an
 * <code>Action</code> in a <code>Controller</code>.
 */
public class ActionController implements Controller {
    private static final long serialVersionUID = -618534639125910093L;

    /**
     * Struts action wrapped.
     */
    private Action action = null;

    /**
     * Constructor.
     *
     * @param action Action to be wrapped.
     */
    public ActionController(Action action) {
        this.action = action;
    }

    /**
     * Method associated to a tile and called immediately before tile is
     * included.  This implementation calls a Struts Action. No servlet is
     * set by this method.
     *
     * @param tileContext    Current tile context.
     * @param request        Current request.
     * @param response       Current response.
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

        try {
            action.execute(null, null, request, response);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * @see org.apache.struts.tiles.Controller#execute(org.apache.struts.tiles.ComponentContext, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.ServletContext)
     */
    public void execute(
            ComponentContext tileContext,
            HttpServletRequest request,
            HttpServletResponse response,
            ServletContext servletContext)
            throws Exception {

        this.action.execute(null, null, request, response);

    }
}
