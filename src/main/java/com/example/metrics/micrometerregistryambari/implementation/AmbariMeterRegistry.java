package com.example.metrics.micrometerregistryambari.implementation;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AmbariMeterRegistry extends StepMeterRegistry {

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new CustomizableThreadFactory("ambari-metrics-publisher");

    private final AmbariConfig config;

    private static final HttpHeaders JSON_HTTP_HEADERS;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    static {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSON_HTTP_HEADERS = HttpHeaders.readOnlyHttpHeaders(headers);
    }


    public AmbariMeterRegistry(AmbariConfig config) {
        super(config, Clock.SYSTEM);
        this.config = config;
        start(DEFAULT_THREAD_FACTORY);
    }

    @PostConstruct
    public void setup() {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public void start(ThreadFactory threadFactory) {
        if (config.enabled()) {
            log.info("Publishing metrics to ambari every " + config.step());
        }
        super.start(threadFactory);
    }

    @Override
    protected void publish() {
        final long timestamp = Instant.now().toEpochMilli();
        List<String> metricNames = new ArrayList<>();
        List<Number> metricValues = new ArrayList<>();

        //TODO: form metric key and value objects considering all types of measurements.
        for (Meter meter : getMeters()) {
            //log.info("Meter ID :-----"+meter.getId().toString());
            for (Measurement measurement : meter.measure()) {
                String metricName = meter.getId().getName().toString()+"."+measurement.getStatistic().toString().toLowerCase();
                //TODO: remove scientific notation from metric values
                BigDecimal metricValue = BigDecimal.valueOf(measurement.getValue());
                metricNames.add(metricName);
                metricValues.add(metricValue);
                //log.info("Meter value:-------"+measurement.getStatistic().toString() + "=" + measurement.getValue());
                log.info(metricName + "=" + metricValue);
            }
        }

        final String json = createJsonObject(timestamp, metricNames, metricValues);
        if (json == null) {
            return; // no data available, try next iteration
        }
        try {
            postMetricObject(json);
        } catch (Exception e) {
            log.error("ERROR occured while sending metrics to Ambari", e);
        }
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.SECONDS;
    }

    private void postMetricObject(String json) {
        HttpEntity<String> httpEntity = new HttpEntity<>(json, JSON_HTTP_HEADERS);
        log.info("Posting :" + httpEntity);
        restTemplate.postForObject(config.ambariMonitoringHost, httpEntity, Object.class);
    }

    private String createJsonObject(long currTime, List<String> metricNames, List<Number> metricValues) {
        final StringBuilder buf = new StringBuilder("{\"metrics\":[\n");
        boolean first = true;
        for (int i = 0, j = metricNames.size(); i < j; i++) {
            final String name = metricNames.get(i);
            final Number value = metricValues.get(i);
            if (value != null) {
                if (!first) {
                    buf.append(",\n");
                } else {
                    first = false;
                }
                buf.append("{\"metricname\": \"");
                if (config.prefix != null) {
                    buf.append(config.prefix + ".");
                }
                buf.append(name);
                buf.append("\",\"appid\": \"").append(config.appId);
                buf.append("\",\"hostname\": \"").append(config.hostName);
                buf.append("\",\"timestamp\": ").append(currTime);
                buf.append(",\"starttime\": ").append(currTime);
                buf.append(",\"metrics\": {\"");
                buf.append(currTime);
                buf.append("\": ");
                buf.append(value.toString());
                buf.append("}}");
            }
        }
        if (first) {
            return null;
        }
        buf.append("\n]}");
        return buf.toString();
    }
}
