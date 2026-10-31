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

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.spring.StrutsInstanceResolver;

/**
 * Action/form instance resolution through Spring: the per-request lookup the
 * engine performs for every action execution and every request-scoped form.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class InstanceResolveBenchmark {

    StrutsInstanceResolver resolver;
    ActionConfig actionConfig;
    FormBeanConfig formConfig;

    @Setup
    public void setup() {
        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(BenchSpringConfig.class);
        resolver = new StrutsInstanceResolver();
        resolver.setApplicationContext(context);

        actionConfig = new ActionConfig();
        actionConfig.setPath("/Bench");
        actionConfig.setType(BenchAction.class.getName());

        formConfig = new FormBeanConfig();
        formConfig.setName("benchForm");
        formConfig.setType(BenchForm.class.getName());
    }

    @Benchmark
    public Action resolveAction() throws Exception {
        return resolver.resolveAction(actionConfig);
    }

    @Benchmark
    public ActionForm resolveForm() throws Exception {
        return resolver.resolveForm(formConfig);
    }
}
