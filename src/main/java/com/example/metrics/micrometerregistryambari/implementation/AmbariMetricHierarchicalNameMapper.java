package com.example.metrics.micrometerregistryambari.implementation;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class AmbariMetricHierarchicalNameMapper implements HierarchicalNameMapper {

    private final List<String> tagsAsPrefix;
    private static final Pattern blacklistedChars = Pattern.compile("[{}(),=\\[\\]/]");

    public AmbariMetricHierarchicalNameMapper(String... tagsAsPrefix) {
        this.tagsAsPrefix = Arrays.asList(tagsAsPrefix);
    }

    @Override
    public String toHierarchicalName(Meter.Id id, NamingConvention convention) {
        StringBuilder hierarchicalName = new StringBuilder();

        for (String tagKey : tagsAsPrefix) {
            String tagValue = id.getTag(tagKey);
            if (tagValue != null) {
                hierarchicalName
                        .append(convention.tagKey(tagKey))
                        .append(".")
                        .append(convention.tagValue(tagValue))
                        .append(".");
            }
        }
        hierarchicalName.append(id.getConventionName(convention));
        for (Tag tag : id.getConventionTags(convention)) {
            if (!tagsAsPrefix.contains(tag.getKey()) && !"statistic".equalsIgnoreCase(tag.getKey())) {
                hierarchicalName
                        .append('.')
                        .append(convention.tagKey(tag.getKey()))
                        .append('.')
                        .append(convention.tagValue(tag.getValue()));
            }
        }

        return sanitize(hierarchicalName.toString());

    }

    private String sanitize(String delegated) {
        return blacklistedChars.matcher(delegated).replaceAll("_")
                .replace(" ", "_")
                .toLowerCase();
    }

}
