package org.apache.struts.bench;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.ModuleConfigFactory;
import org.apache.struts.spring.StrutsHandlerAdapter;
import org.apache.struts.spring.StrutsInstanceResolver;

/**
 * Full per-request pipeline of the engine on spring-test mocks: handler
 * adapter handle() runs locale/session processing, form creation through the
 * Spring context, populate, execute and forward through the mock servlet
 * context dispatcher.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class RequestPipelineBenchmark {

    StrutsHandlerAdapter adapter;

    @Setup
    public void setup() {
        ModuleConfig moduleConfig = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        FormBeanConfig formBeanConfig = new FormBeanConfig();
        formBeanConfig.setName("benchForm");
        formBeanConfig.setType(BenchForm.class.getName());
        moduleConfig.addFormBeanConfig(formBeanConfig);

        ActionMapping mapping = new ActionMapping();
        mapping.setPath("/Bench");
        mapping.setName("benchForm");
        mapping.setScope("request");
        mapping.setValidate(false);
        mapping.setType(BenchAction.class.getName());
        mapping.setModuleConfig(moduleConfig);
        this.mapping = mapping;

        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(BenchSpringConfig.class);

        ActionServlet actionServlet = new ActionServlet();
        StrutsInstanceResolver resolver = new StrutsInstanceResolver();
        resolver.setApplicationContext(context);

        // MockServletContext.getRequestDispatcher returns a recording
        // MockRequestDispatcher for any path, so no registration is needed.
        MockServletContext servletContext = new MockServletContext();

        adapter = new StrutsHandlerAdapter(actionServlet, resolver);
        adapter.setServletContext(servletContext);
    }

    ActionMapping mapping;

    @Benchmark
    public void handleFullPipeline() throws Exception {
        MockHttpServletRequest request =
            new MockHttpServletRequest("GET", "/Bench.do");
        request.addParameter("firstName", "Ivan");
        request.addParameter("lastName", "Petrov");
        request.addParameter("email", "ivan@example.com");
        request.addParameter("city", "Moscow");
        request.addParameter("phone", "+7-000-000-00-00");
        request.addParameter("age", "33");
        MockHttpServletResponse response = new MockHttpServletResponse();
        adapter.handle(request, response, mapping);
    }
}
