package dev.openfeature.sdk.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;
import org.openjdk.jmh.util.Utils;

/**
 * Takes a heap dump (using JMAP from a separate process) after a benchmark;
 * only useful if GC is disabled during the benchmark.
 */
public class AllocationProfiler implements InternalProfiler {

    public static class AllocationTotals {
        long instances;
        long bytes;

        public AllocationTotals(long instances, long bytes) {
            this.instances = instances;
            this.bytes = bytes;
        }
    }

    @Override
    public String getDescription() {
        return "Max memory heap profiler";
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        // intentionally left blank
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
            IterationResult result) {

        long totalHeap = Runtime.getRuntime().totalMemory();
        AllocationTotals allocationTotals = AllocationProfiler.printHeapHistogram(System.out, 120);

        Collection<ScalarResult> results = new ArrayList<>();
        results.add(new ScalarResult("+totalHeap", totalHeap, "bytes", AggregationPolicy.MAX));
        results.add(new ScalarResult("+totalAllocatedInstances", allocationTotals.instances, "instances",
                AggregationPolicy.MAX));
        results.add(new ScalarResult("+totalAllocatedBytes", allocationTotals.bytes, "bytes", AggregationPolicy.MAX));

        return results;
    }

    private static String getJmapExcutable() {
        String javaHome = System.getProperty("java.home");
        String jreDir = File.separator + "jre";
        if (javaHome.endsWith(jreDir)) {
            javaHome = javaHome.substring(0, javaHome.length() - jreDir.length());
        }
        return (javaHome +
                File.separator +
                "bin" +
                File.separator +
                "jmap" +
                (Utils.isWindows() ? ".exe" : ""));
    }

    // runs JMAP executable in a new process to collect a heap dump
    // heavily inspired by: https://github.com/cache2k/cache2k-benchmark/blob/master/jmh-suite/src/main/java/org/cache2k/benchmark/jmh/HeapProfiler.java
    private static AllocationTotals printHeapHistogram(PrintStream out, int maxLines) {
        long totalBytes = 0;
        long totalInstances = 0;
        boolean partial = false;
        try {
            Process jmapProcess = Runtime.getRuntime().exec(new String[] {
                    getJmapExcutable(),
                    "-histo:live",
                    Long.toString(Utils.getPid()) });
            InputStream in = jmapProcess.getInputStream();
            LineNumberReader r = new LineNumberReader(new InputStreamReader(in));
            String line;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(buffer);
            while ((line = r.readLine()) != null) {
                if (line.startsWith("Total")) {
                    printStream.println(line);
                    String[] tokens = line.split("\\s+");
                    totalInstances += Long.parseLong(tokens[1]);
                    totalBytes = Long.parseLong(tokens[2]);
                } else if (r.getLineNumber() <= maxLines) {
                    printStream.println(line);
                } else {
                    if (!partial) {
                        printStream.println("truncated...");
                    }
                    partial = true;
                }
            }
            r.close();
            in.close();
            printStream.close();
            byte[] histogramOutput = buffer.toByteArray();
            buffer = new ByteArrayOutputStream();
            printStream = new PrintStream(buffer);
            printStream.write(histogramOutput);
            printStream.println();
            printStream.close();
            out.write(buffer.toByteArray());
        } catch (Exception ex) {
            System.err.println("ForcedGcMemoryProfiler: error attaching / reading histogram");
            ex.printStackTrace();
        }
        return new AllocationTotals(totalInstances, totalBytes);
    }
}