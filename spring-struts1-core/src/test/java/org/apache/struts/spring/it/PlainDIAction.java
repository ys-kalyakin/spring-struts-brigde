package org.apache.struts.spring.it;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Unannotated action declared only in struts-config: the registrar must
 * register it as a Spring singleton, so the dependency is injected and
 * {@code @PostConstruct} runs before the first execute.
 */
public class PlainDIAction extends Action {

    @Autowired
    private transient GreeterService greeterService;

    private final AtomicInteger postConstructCount = new AtomicInteger();

    @PostConstruct
    void postConstruct() {
        postConstructCount.incrementAndGet();
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("plaindi:"
            + Integer.toHexString(System.identityHashCode(this))
            + ":" + greeterService.greet()
            + ":postConstruct=" + postConstructCount.get());
        return null;
    }
}
