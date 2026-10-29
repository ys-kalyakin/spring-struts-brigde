package org.apache.struts.tiles.it;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.Controller;

/**
 * Test controller referenced from the tiles-defs fixtures: adds an attribute
 * to the tile context (the classic pre-include controller contract) and
 * echoes selected context contents into the response so tests can verify
 * that the sub-context was built and passed to the tile.
 */
public class TestTileController implements Controller {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(ComponentContext tileContext, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext)
        throws IOException, ServletException {
        // the classic controller side effect: modify the tile context
        // before the tile is included
        Object items = tileContext.getAttribute("items");
        String extra = items instanceof List
            ? "items:" + ((List<?>) items).size()
            : "added-by-controller";
        tileContext.putAttribute("extra", extra);

        response.getWriter().write("TITLE:" + tileContext.getAttribute("title"));
        response.getWriter().write(";BODY:" + tileContext.getAttribute("body"));
    }

    @Override
    @Deprecated
    public void perform(ComponentContext tileContext, HttpServletRequest request,
        HttpServletResponse response, ServletContext servletContext)
        throws IOException, ServletException {
        execute(tileContext, request, response, servletContext);
    }
}
