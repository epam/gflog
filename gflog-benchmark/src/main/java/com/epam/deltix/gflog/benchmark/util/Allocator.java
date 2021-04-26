package com.epam.deltix.gflog.benchmark.util;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


public final class Allocator implements Sampler {

    private final Lookup lookup = new Lookup(4096);

    private boolean active = false;

    private Allocator() {
    }

    @Override
    public synchronized void sampleAllocation(final int arrayLength,
                                              final String description,
                                              final Object instance,
                                              final long memory) {
        if (active) {
            final Class<?> clazz = instance.getClass();
            final Stat stat = lookup.lookup(clazz);

            stat.count++;
            stat.sum += memory;
        }
    }

    public synchronized String toFootprint() {
        final ArrayList<Stat> allocations = lookup.toList();

        if (allocations.isEmpty()) {
            return "N/A - to enable: -javaagent:" + Location.LOCATION;
        }

        allocations.removeIf(stat -> stat.clazz == Tracer.class);

        final ArrayList<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(String.format("%15s%15s%15s   %s", "COUNT", "AVG", "SUM", "Class"));

        final Comparator<Stat> comparator = Comparator.comparingLong(stat -> stat.sum);
        final Counter totalCount = new Counter();
        final Counter totalSum = new Counter();

        allocations
                .stream()
                .sorted(comparator.reversed())
                .forEach(stat -> {
                    final String clazz = stat.clazz.getCanonicalName();
                    final long count = stat.count;
                    final long sum = stat.sum;
                    final long avg = sum / count;

                    totalCount.value += count;
                    totalSum.value += sum;

                    lines.add(String.format("%15d%15d%15d   %s", count, avg, sum, clazz));
                });

        lines.add(String.format("%15d%15s%15d   %s", totalCount.value, "", totalSum.value, "(total)"));

        final String separator = System.lineSeparator();
        return String.join(separator, lines);
    }

    public synchronized void start() {
        lookup.clear();
        active = true;
        new Tracer();
    }

    public synchronized void stop() {
        active = false;
    }

    public synchronized void uninstall() {
        stop();
        AllocationRecorder.removeSampler(this);
    }

    public static Allocator install() {
        new Tracer();

        final Allocator allocator = new Allocator();
        AllocationRecorder.addSampler(allocator);

        return allocator;
    }

    private static final class Counter {

        private long value;

    }

    private static final class Stat {

        private final Class<?> clazz;

        private long count;
        private long sum;

        private Stat(final Class<?> clazz) {
            this.clazz = clazz;
        }

    }

    private static final class Lookup {

        private final Stat[] array;

        private Lookup(final int capacity) {
            this.array = new Stat[capacity];
        }

        public Stat lookup(final Class<?> clazz) {
            final Stat[] array = this.array;
            final int mask = array.length - 1;
            final int hash = clazz.hashCode();

            int index = hash & mask;
            Stat stat = null;

            for (int i = 0; i <= mask; i++) {
                stat = array[index];

                if (stat == null) {
                    stat = new Stat(clazz);
                    array[index] = stat;
                    break;
                }

                if (stat.clazz == clazz) {
                    break;
                }

                index = (index + 1) & mask;
            }

            if (stat == null) {
                throw new IllegalStateException("All slots are occupied");
            }

            return stat;
        }

        public void clear() {
            Arrays.fill(array, null);
        }

        public ArrayList<Stat> toList() {
            final ArrayList<Stat> list = new ArrayList<>();

            for (final Stat stat : array) {
                if (stat != null) {
                    list.add(stat);
                }
            }

            return list;
        }

    }

    private static final class Tracer {
    }

    private static final class Location {

        private static final String LOCATION;

        static {
            String location = "path-to/java-allocation-instrumenter-3.3.0.jar";

            try {
                final URI uri = Sampler.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI();

                location = new File(uri).getAbsolutePath();
            } catch (final URISyntaxException e) {
                // ignore
            }

            LOCATION = location;
        }

    }

}
