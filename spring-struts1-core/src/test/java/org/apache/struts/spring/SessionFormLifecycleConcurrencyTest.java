package org.apache.struts.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.impl.ModuleConfigImpl;
import org.apache.struts.util.RequestUtils;

/**
 * Regression tests of the CVE-2016-1181 window: for session-scoped forms,
 * acquiring the instance, running <code>reset()</code> and populating it —
 * on regular and multipart requests alike — are serialized per session, so
 * one request's populate never interleave with the other's, and the
 * <code>html:form</code> render path cannot swap the stored instance under a
 * populate. The populate window is deliberately widened with a sleep in a
 * setter; the race runs repeatedly to make a pre-fix interleaving likely.
 */
class SessionFormLifecycleConcurrencyTest {

    private static final int RACE_ITERATIONS = 20;

    /** A form recording its reset and setter calls with the caller thread. */
    public static class RecordingForm extends ActionForm {

        final List<String> events =
            Collections.synchronizedList(new ArrayList<>());

        private String name;

        private String color;

        @Override
        public void reset(ActionMapping mapping, HttpServletRequest request) {
            events.add("reset@" + Thread.currentThread().getName());
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            events.add("setName@" + Thread.currentThread().getName());
            // widen the populate window so a pre-fix interleaving is likely
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            events.add("setColor@" + Thread.currentThread().getName());
            this.color = color;
        }
    }

    public static class RecordingAction extends Action {

        @Override
        public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
            return null;
        }
    }

    private final AnnotationConfigWebApplicationContext context =
        new AnnotationConfigWebApplicationContext();

    private StrutsHandlerAdapter adapter;

    private ActionMapping mapping;

    private ModuleConfigImpl module;

    private void initEngine() {
        context.refresh();
        ActionServlet servlet = new ActionServlet();
        servlet.setApplicationContext(context);
        StrutsInstanceResolver resolver = new StrutsInstanceResolver();
        resolver.setApplicationContext(context);
        adapter = new StrutsHandlerAdapter(servlet, resolver);
        adapter.setServletContext(new MockServletContext());

        module = new ModuleConfigImpl("");
        FormBeanConfig formConfig = new FormBeanConfig();
        formConfig.setName("recForm");
        formConfig.setType(RecordingForm.class.getName());
        module.addFormBeanConfig(formConfig);

        mapping = new ActionMapping();
        mapping.setPath("/rec");
        mapping.setName("recForm");
        mapping.setScope("session");
        mapping.setType(RecordingAction.class.getName());
        module.addActionConfig(mapping);
    }

    @Test
    void concurrentPopulateRequestsDoNotInterleaveOnTheSessionForm()
        throws Exception {
        initEngine();
        for (int i = 0; i < RACE_ITERATIONS; i++) {
            raceRegularAgainstMultipart(i);
        }
    }

    private void raceRegularAgainstMultipart(int iteration) throws Exception {
        MockHttpSession session = new MockHttpSession();

        MockHttpServletRequest regular = new MockHttpServletRequest("POST",
            "/rec.do");
        regular.setSession(session);
        regular.setAttribute(Globals.MODULE_KEY, module);
        regular.setParameter("name", "Alice");
        regular.setParameter("color", "red");

        MockMultipartHttpServletRequest multipart =
            new MockMultipartHttpServletRequest();
        multipart.setMethod("POST");
        multipart.setContentType("multipart/form-data; boundary=mock");
        multipart.setSession(session);
        multipart.setAttribute(Globals.MODULE_KEY, module);
        multipart.setParameter("name", "Bob");
        multipart.setParameter("color", "blue");
        multipart.addFile(new MockMultipartFile("unrelated", "f.txt",
            "text/plain", "x".getBytes()));

        CyclicBarrier barrier = new CyclicBarrier(2);
        List<Throwable> failures =
            Collections.synchronizedList(new ArrayList<>());
        Thread regularThread = new Thread(() -> run(iteration, barrier,
            failures, regular), "race-regular-" + iteration);
        Thread multipartThread = new Thread(() -> run(iteration, barrier,
            failures, multipart), "race-multipart-" + iteration);

        regularThread.start();
        multipartThread.start();
        regularThread.join(10_000);
        multipartThread.join(10_000);

        assertTrue(failures.isEmpty(), () -> failures.toString());
        RecordingForm form = (RecordingForm) session.getAttribute("recForm");
        assertTrue(form != null, "the session form was stored");
        // both threads recorded into the same form: a second, discarded
        // instance would have its own event list and the interleaving
        // check below would miss one thread's reset
        List<String> events = new ArrayList<>(form.events);
        assertNoInterleaving(events, regularThread.getName(),
            multipartThread.getName(), iteration);
    }

    private void run(int iteration, CyclicBarrier barrier,
        List<Throwable> failures, HttpServletRequest request) {
        try {
            barrier.await();
            adapter.handle(request, new MockHttpServletResponse(), mapping);
        } catch (Throwable t) {
            failures.add(new AssertionError(iteration + ": " + t, t));
        }
    }

    /**
     * Between a thread's reset and its last recorded event no event of the
     * other thread may appear: reset and populate are atomic per session.
     */
    private void assertNoInterleaving(List<String> events, String first,
        String second, int iteration) {
        assertTrue(events.contains("reset@" + first), () -> events.toString());
        assertTrue(events.contains("reset@" + second), () -> events.toString());
        for (String thread : new String[] {first, second}) {
            int resetIndex = events.indexOf("reset@" + thread);
            int lastIndex = lastIndexOfThread(events, thread);
            assertTrue(lastIndex > resetIndex,
                () -> iteration + ": " + events);
            for (int i = resetIndex + 1; i <= lastIndex; i++) {
                assertEquals(thread,
                    events.get(i).substring(events.get(i).indexOf('@') + 1),
                    () -> iteration + ": a foreign thread mutated the form "
                        + "inside " + thread + "'s populate window: "
                        + events);
            }
        }
    }

    private int lastIndexOfThread(List<String> events, String thread) {
        int last = -1;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).endsWith("@" + thread)) {
                last = i;
            }
        }
        return last;
    }

    @Test
    void renderPathCannotSwapTheStoredInstanceUnderAPopulate()
        throws Exception {
        initEngine();
        MockHttpSession session = new MockHttpSession();

        MockHttpServletRequest first = renderRequest(session);
        MockHttpServletRequest second = renderRequest(session);

        CyclicBarrier barrier = new CyclicBarrier(2);
        List<Throwable> failures =
            Collections.synchronizedList(new ArrayList<>());
        Thread firstThread = new Thread(() -> create(barrier, failures,
            first), "render-first");
        Thread secondThread = new Thread(() -> create(barrier, failures,
            second), "render-second");
        firstThread.start();
        secondThread.start();
        firstThread.join(10_000);
        secondThread.join(10_000);

        assertTrue(failures.isEmpty(), () -> failures.toString());
        ActionForm stored = (ActionForm) session.getAttribute("recForm");
        assertSame(stored, first.getAttribute("recForm"),
            "the render path stores exactly one shared instance");
        assertSame(stored, second.getAttribute("recForm"),
            "both concurrent renders resolved the same stored instance");
    }

    private MockHttpServletRequest renderRequest(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
            "/rec.do");
        request.setSession(session);
        return request;
    }

    private void create(CyclicBarrier barrier, List<Throwable> failures,
        MockHttpServletRequest request) {
        try {
            barrier.await();
            ActionForm form = RequestUtils.createActionForm(request, mapping,
                module, null);
            request.setAttribute("recForm", form);
        } catch (Throwable t) {
            failures.add(new AssertionError(t));
        }
    }
}
