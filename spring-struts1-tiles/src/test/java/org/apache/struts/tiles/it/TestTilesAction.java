package org.apache.struts.tiles.it;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;

/**
 * Fixture action extending the classic TilesAction: invoked as the content
 * of an inserted tile (the definition path points at the action URL), so a
 * tile context exists when it runs — the classic guard otherwise rejects
 * top-level invocations. It forwards to a plain JSP which reads the tile
 * context.
 */
public class TestTilesAction extends TilesAction {

    @Override
    public ActionForward execute(ComponentContext context,
        ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        return new ActionForward("/tiles-action.jsp");
    }
}
