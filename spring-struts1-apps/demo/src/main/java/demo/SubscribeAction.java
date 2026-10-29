package demo;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.StrutsAction;

/**
 * The subscription action of the demo: a Spring singleton bean
 * ({@link StrutsAction}) with an injected dependency — re-used across
 * requests like a classic singleton action.
 */
@StrutsAction
public class SubscribeAction extends Action {

    private final String instanceId = UUID.randomUUID().toString();

    private final SubscribeService subscribeService;

    public SubscribeAction(SubscribeService subscribeService) {
        this.subscribeService = subscribeService;
    }

    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        SubscribeForm subscribeForm = (SubscribeForm) form;
        request.setAttribute("actionInstanceId", instanceId);
        request.setAttribute("formInstanceId", subscribeForm.getInstanceId());
        request.setAttribute("greeting", subscribeService.subscribe(
            subscribeForm.getName(), subscribeForm.getEmail()));
        return mapping.findForward("success");
    }
}
