package org.apache.struts.bench;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import org.apache.struts.util.MessageResources;
import org.apache.struts.util.PropertyMessageResourcesFactory;

/**
 * Message lookup cost: a plain key and the parametric-replacement path
 * (MessageFormat cache and formatting), both per-request hot spots of the
 * tag libraries and error rendering.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class MessageResourcesBenchmark {

    MessageResources resources;

    @Setup
    public void setup() {
        PropertyMessageResourcesFactory factory =
            new PropertyMessageResourcesFactory();
        factory.setReturnNull(false);
        resources = factory
            .createResources("org.apache.struts.bench.BenchmarkMessages");
        // Warm the message/format caches so the benchmark measures steady
        // lookups, not first-load file I/O.
        resources.getMessage(Locale.ENGLISH, "bench.plain");
        resources.getMessage(Locale.ENGLISH, "bench.args", "Ivan", 33);
    }

    @Benchmark
    public String lookupPlain() {
        return resources.getMessage(Locale.ENGLISH, "bench.plain");
    }

    @Benchmark
    public String formatTwoArgs() {
        return resources.getMessage(Locale.ENGLISH, "bench.args",
            new Object[] { "Ivan", 33 });
    }
}
