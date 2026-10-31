package org.apache.struts.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.util.RequestUtils;

/**
 * Populate cost in isolation: scalar parameter assignment and the indexed
 * expression path (<code>items[0]</code>-style names) through the engine's
 * populate machinery.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class FormPopulateBenchmark {

    /** Form with twelve scalar properties, an array and a list property. */
    public static class PopulateForm extends ActionForm {

        private String p0;
        private String p1;
        private String p2;
        private String p3;
        private String p4;
        private String p5;
        private String p6;
        private String p7;
        private String p8;
        private String p9;
        private Integer p10;
        private Integer p11;
        private String[] items;
        private List<String> tags = new ArrayList<>();

        public String getP0() {
            return p0;
        }

        public void setP0(String p0) {
            this.p0 = p0;
        }

        public String getP1() {
            return p1;
        }

        public void setP1(String p1) {
            this.p1 = p1;
        }

        public String getP2() {
            return p2;
        }

        public void setP2(String p2) {
            this.p2 = p2;
        }

        public String getP3() {
            return p3;
        }

        public void setP3(String p3) {
            this.p3 = p3;
        }

        public String getP4() {
            return p4;
        }

        public void setP4(String p4) {
            this.p4 = p4;
        }

        public String getP5() {
            return p5;
        }

        public void setP5(String p5) {
            this.p5 = p5;
        }

        public String getP6() {
            return p6;
        }

        public void setP6(String p6) {
            this.p6 = p6;
        }

        public String getP7() {
            return p7;
        }

        public void setP7(String p7) {
            this.p7 = p7;
        }

        public String getP8() {
            return p8;
        }

        public void setP8(String p8) {
            this.p8 = p8;
        }

        public String getP9() {
            return p9;
        }

        public void setP9(String p9) {
            this.p9 = p9;
        }

        public Integer getP10() {
            return p10;
        }

        public void setP10(Integer p10) {
            this.p10 = p10;
        }

        public Integer getP11() {
            return p11;
        }

        public void setP11(Integer p11) {
            this.p11 = p11;
        }

        public String[] getItems() {
            return items;
        }

        public void setItems(String[] items) {
            this.items = items;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    PopulateForm form;

    @Setup
    public void setup() {
        form = new PopulateForm();
    }

    @Benchmark
    public void populateScalar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
            "/populate");
        request.addParameter("p0", "v0");
        request.addParameter("p1", "v1");
        request.addParameter("p2", "v2");
        request.addParameter("p3", "v3");
        request.addParameter("p4", "v4");
        request.addParameter("p5", "v5");
        request.addParameter("p6", "v6");
        request.addParameter("p7", "v7");
        request.addParameter("p8", "v8");
        request.addParameter("p9", "v9");
        request.addParameter("p10", "10");
        request.addParameter("p11", "11");
        RequestUtils.populate(form, null, null, request);
    }

    @Benchmark
    public void populateIndexed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
            "/populate");
        request.addParameter("items[0]", "a");
        request.addParameter("items[1]", "b");
        request.addParameter("items[2]", "c");
        request.addParameter("tags[0]", "x");
        request.addParameter("tags[1]", "y");
        request.addParameter("tags[2]", "z");
        RequestUtils.populate(form, null, null, request);
    }
}
