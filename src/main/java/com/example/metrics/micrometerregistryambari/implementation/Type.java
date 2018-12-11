package com.example.metrics.micrometerregistryambari.implementation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Type {
    GAUGE("gauge");

    private final String value;

    Type(String value) {
        this.value = value;
    }

    @JsonValue
    String getValue() {
        return this.value;
    }
}
