package com.example.metrics.micrometerregistryambari.implementation;

import io.micrometer.core.instrument.step.StepRegistryConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface AmbariConfig extends StepRegistryConfig {

    default String metricCollectorUrl() {
        return "http://localhost:6188/ws/v1/timeline/metrics";
    }

    default String hostname() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            // If not FQDN , call DNS
            if ((hostName == null) || (!hostName.contains("."))) {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            }
        } catch (UnknownHostException e) {
            System.out.println("Could not identify hostname."+ e);
        }
        return hostName;
    }

    default String[] tagsAsPrefix() {
        return new String[0];
    }
}
