package org.gangel.orders.grpc.common;

import lombok.Getter;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

public class Histogram {

    private final static TemporalUnit PRECISION_UNITS = ChronoUnit.NANOS;
    
    @Getter
    private TemporalUnit units = PRECISION_UNITS;

    public static class Statistics {
        public Duration p50th = Duration.ofNanos(0);
        public Duration p99th = Duration.ofNanos(0);
        public Duration p99d9th = Duration.ofNanos(0);
        public Duration p99d99th = Duration.ofNanos(0);
        public Duration p99d999th = Duration.ofNanos(0);
        public Duration p100th = Duration.ofNanos(0); // worst case
        public Duration totalTime = Duration.ofNanos(0);
        
        public Statistics() {
        }
        
        @Override
        public String toString() {
            return String.format("percentiles 50th=%.3f; 99th=%.3f; 99.9th=%.3f; 99.99th=%.3f; 99.999th=%.3f; 100th=%.3f; totalTimeSec=%.3f;", 
                    convertToMils(p50th), 
                    convertToMils(p99th), 
                    convertToMils(p99d9th), 
                    convertToMils(p99d99th), 
                    convertToMils(p99d999th), 
                    convertToMils(p100th), 
                    1e-3 * totalTime.toMillis());
        }
        
        public double convertToMils(Duration duration) {
            return  (double) duration.toNanos() / (double)Duration.of(1, ChronoUnit.MILLIS).toNanos();        
        }
        
    }
    
    private int maxSamples = 10_000;
    
    private Deque<Long> samples;
    
    public long totalTime = 0;
    
    public long worstTime = 0;
    
    public Histogram(int nSamples, TemporalUnit units) {
        this.maxSamples = nSamples;
        this.samples = new ArrayDeque<Long>(nSamples);
        this.units = units; 
    }
    
    public Histogram(int nSamples) {
        this(nSamples, PRECISION_UNITS);
    }
    
    public void put(long value) {
        samples.add(value);
        if (samples.size() > maxSamples) {
           samples.remove();
        }
        totalTime += value;
        worstTime = Math.max(worstTime, value);
    }
    
    public Collection<Long> getData() {
        return Collections.unmodifiableCollection(this.samples);
    }

    public static Histogram.Statistics getStats(Histogram histogram, Percentile.EstimationType estimationType) {
        ArrayList<Histogram> data = new ArrayList<>(1);
        data.add(histogram);
        return getStats(data);
    }
    
    public static long convert(long value, TemporalUnit sourceUnit, TemporalUnit targetUnit) {
        if (sourceUnit.equals(targetUnit)) {
            return value; 
        }
        long v = value * sourceUnit.getDuration().toNanos(); // convert to ns
        v /= targetUnit.getDuration().toNanos(); // convert to target unit
        return v;
    }

    public static Histogram.Statistics getStats(Histogram histogram) {
        return getStats(histogram, Percentile.EstimationType.R_7);
    }
    
    public static Histogram.Statistics getStats(Collection<Histogram> histograms) {
        return getStats(histograms, Percentile.EstimationType.R_7);
    }

    public static Histogram.Statistics getStats(Collection<Histogram> histograms, Percentile.EstimationType estimationType) {
        Statistics s = new Statistics();
        
        int totalSize = 0;
        long totalTime = 0L;
        long worstTime = 0L;
        Iterator<Histogram> iterator = histograms.iterator();
        while (iterator.hasNext()) {
            Histogram h = iterator.next();
            totalSize += h.samples.size();
            totalTime += convert(h.totalTime, h.units, PRECISION_UNITS);
            worstTime = Math.max(worstTime, convert(h.worstTime, h.units, PRECISION_UNITS));
        }
        
        double allData[] = new double[totalSize];

        iterator = histograms.iterator();
        int pos = 0;
        while (iterator.hasNext()) {
            Histogram h = iterator.next();
            for (long value : h.samples) {
                allData[pos++] = value;
            }
        }

        Percentile p = new Percentile().withEstimationType(estimationType);
        p.setData(allData);
        s.p50th = Duration.of((long)p.evaluate(50), PRECISION_UNITS);
        s.p99th = Duration.of((long)p.evaluate(99), PRECISION_UNITS);
        s.p99d9th = Duration.of((long)p.evaluate(99.9), PRECISION_UNITS);
        s.p99d99th = Duration.of((long)p.evaluate(99.99), PRECISION_UNITS);
        s.p99d999th = Duration.of((long)p.evaluate(99.999), PRECISION_UNITS);
        s.p100th = Duration.of(worstTime, PRECISION_UNITS);
        s.totalTime = Duration.of(totalTime, PRECISION_UNITS);
        return s; 
    }
}
