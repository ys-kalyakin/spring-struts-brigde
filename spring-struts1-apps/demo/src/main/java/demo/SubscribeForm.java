package demo;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.StrutsForm;

/**
 * The subscription form of the demo: a Spring prototype bean
 * ({@link StrutsForm}) — every request receives its own instance, the
 * validation follows the classic <code>validate()</code> contract.
 */
@StrutsForm
public class SubscribeForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    private final String instanceId = UUID.randomUUID().toString();

    private String name;

    private String email;

    public String getInstanceId() {
        return instanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (name == null || name.isBlank()) {
            errors.add("name",
                new ActionMessage("error.name.required"));
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            errors.add("email",
                new ActionMessage("error.email.required"));
        }
        return errors;
    }
}
