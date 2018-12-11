package com.example.metrics.micrometerregistryambari.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

public class Metric {
    private final String name;

    private final Map<String, String> tags;

    private final Long timestamp;

    private final Type type;

    private final String unit;

    private final Number value;

    /**
     * Creates a new instance
     *
     * @param name      the name of the metric
     * @param tags      the tags associated with the metric
     * @param timestamp the timestamp of the metric
     * @param type      the type of the metric
     * @param unit      the unit of the metric value
     * @param value     the metric value
     */
    public Metric(String name, Map<String, String> tags, Long timestamp, Type type, String unit, Number value) {
        Assert.notNull(name, "name must not be null");
        Assert.notNull(tags, "tags must not be null");
        Assert.notNull(timestamp, "timestamp must not be null");
        Assert.notNull(type, "type must not be null");
        Assert.notNull(value, "value must not be null");

        this.name = name;
        this.tags = tags;
        this.timestamp = timestamp;
        this.type = type;
        this.unit = unit;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return name.equals(metric.name) &&
                tags.equals(metric.tags) &&
                timestamp.equals(metric.timestamp) &&
                type == metric.type &&
                Objects.equals(unit, metric.unit) &&
                value.equals(metric.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tags, timestamp, type, unit, value);
    }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", tags=" + tags +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", unit='" + unit + '\'' +
                ", value=" + value +
                '}';
    }

    @JsonProperty("name")
    String getName() {
        return this.name;
    }

    @JsonProperty("tags")
    Map<String, String> getTags() {
        return this.tags;
    }

    @JsonProperty("timestamp")
    Long getTimestamp() {
        return this.timestamp;
    }

    @JsonProperty("type")
    Type getType() {
        return this.type;
    }

    @JsonProperty("unit")
    String getUnit() {
        return this.unit;
    }

    @JsonProperty("value")
    Number getValue() {
        return this.value;
    }

}
