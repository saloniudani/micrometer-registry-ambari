package com.example.metrics.micrometerregistryambari.implementation;

import java.util.ArrayList;
import java.util.List;

public class AmbariMetricCache {

    private final List<Metric> cache = new ArrayList<>();

    private final Object monitor = new Object();

    boolean addAll(List<Metric> items) {
        synchronized (this.monitor) {
            return this.cache.addAll(items);
        }
    }

    List<Metric> getAndClear() {
        synchronized (this.monitor) {
            List<Metric> items = new ArrayList<>(this.cache);
            this.cache.clear();
            return items;
        }
    }
}
