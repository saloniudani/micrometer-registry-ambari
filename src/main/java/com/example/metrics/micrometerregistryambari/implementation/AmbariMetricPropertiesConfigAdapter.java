package com.example.metrics.micrometerregistryambari.implementation;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesConfigAdapter;

class AmbariMetricPropertiesConfigAdapter extends
        StepRegistryPropertiesConfigAdapter<AmbariMetricProperties> implements AmbariConfig {

    AmbariMetricPropertiesConfigAdapter(AmbariMetricProperties properties) {
        super(properties);
    }


    @Override
    public String metricCollectorUrl() {
        return get(AmbariMetricProperties::getMetricCollectorUrl, AmbariConfig.super::metricCollectorUrl);
    }
}
