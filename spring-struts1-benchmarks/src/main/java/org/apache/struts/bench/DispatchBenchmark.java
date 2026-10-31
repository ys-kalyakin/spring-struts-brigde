package org.apache.struts.bench;

import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.extras.actions.DispatchAction;

/**
 * Dispatch-action cost: the full execute() path of the classic dispatch
 * pattern — method name from the request parameter, reflection method cache
 * lookup and invocation.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class DispatchBenchmark {

    /** Concrete dispatch action with the benchmark target method. */
    public static class BenchDispatchAction extends DispatchAction {

        public ActionForward bench(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
            return new ActionForward("/bench-result.jsp");
        }
    }

    BenchDispatchAction action;
    ActionMapping mapping;

    @Setup
    public void setup() {
        action = new BenchDispatchAction();
        mapping = new ActionMapping();
        mapping.setPath("/Dispatch");
        mapping.setParameter("method");
    }

    @Benchmark
    public Object dispatchByParameter() throws Exception {
        MockHttpServletRequest request =
            new MockHttpServletRequest("GET", "/Dispatch.do");
        request.addParameter("method", "bench");
        MockHttpServletResponse response = new MockHttpServletResponse();
        return action.execute(mapping, null, request, response);
    }
}
