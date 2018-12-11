package com.example.metrics.micrometerregistryambari.implementation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AmbariMetricPublisher {

    private static final String RETRY_AFTER = "X-RateLimit-Retry-After";
    private static final HttpHeaders JSON_HTTP_HEADERS;

    private final AmbariMetricCache cache = new AmbariMetricCache();

    private final AmbariMetricProperties properties;

    private final RestTemplate restTemplate;

    private volatile Long retryAfter;

    static {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSON_HTTP_HEADERS = HttpHeaders.readOnlyHttpHeaders(headers);
    }

    AmbariMetricPublisher(AmbariMetricProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public void publish(List<Metric> metrics) {
        if (this.retryAfter != null && this.retryAfter > System.currentTimeMillis()) {
            log.info("Skipping sending Spring Boot metrics to AMS");
            return;
        }

        log.info("Sending Spring Boot metrics to AMS");

        try {
            this.restTemplate.postForObject(this.properties.getMetricCollectorUrl(), getRequest(getAllMetrics(metrics)), Object.class);
            log.info("Sent Spring Boot metrics to AMS");
            this.retryAfter = null;
        } catch (Exception e) {
            if (e instanceof HttpStatusCodeException) {
                HttpStatus statusCode = ((HttpStatusCodeException) e).getStatusCode();

                if (HttpStatus.UNPROCESSABLE_ENTITY == statusCode) {
                    log.error("Failed to send Spring Boot metrics to AMS due to unprocessable payload.  Discarding metrics.", e);
                } else if (HttpStatus.PAYLOAD_TOO_LARGE == statusCode) {
                    log.error("Failed to send Spring Boot metrics to AMS due to rate limiting.  Discarding metrics.", e);
                } else if (HttpStatus.TOO_MANY_REQUESTS == statusCode) {
                    log.error("Failed to send Spring Boot metrics to AMS due to rate limiting.  Caching metrics.", e);
                    this.cache.addAll(metrics);
                } else {
                    log.error("Failed to send Spring Boot metrics to AMS. Caching metrics.", e);
                    this.cache.addAll(metrics);
                }
            }else{
                log.error("Failed to send Spring Boot metrics to AMS.",e);
            }
            this.retryAfter = getRetryAfter(e);
        }
    }

    private List<Metric> getAllMetrics(List<Metric> newMetrics) {
        List<Metric> allMetrics = this.cache.getAndClear();
        allMetrics.addAll(newMetrics);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Sending metrics: %s", allMetrics));
        }

        return allMetrics;
    }

    private String getPayload(List<Metric> metrics) {
        final StringBuilder buf = new StringBuilder("{\"metrics\":[\n");
        boolean first = true;
        for (Metric metric : metrics) {
            //TODO: Dimensionality reduction
            final String name = metric.getName();
            final Number value = metric.getValue();
            if (value != null) {
                if (!first) {
                    buf.append(",\n");
                } else {
                    first = false;
                }
                buf.append("{\"metricname\": \"");
                buf.append(name);
                buf.append("\",\"appid\": \"").append(metric.getTags().get("app"));
                buf.append("\",\"hostname\": \"").append(properties.getHostname());
                buf.append("\",\"timestamp\": ").append(metric.getTimestamp());
                buf.append(",\"starttime\": ").append(metric.getTimestamp());
                buf.append(",\"metrics\": {\"");
                buf.append(metric.getTimestamp());
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

    private HttpEntity<String> getRequest(List<Metric> metrics) {
        return new HttpEntity<>(getPayload(metrics), JSON_HTTP_HEADERS);
    }

    private Long getRetryAfter(Exception candidate) {
        if (candidate instanceof RestClientResponseException) {
            String retryAfter = ((RestClientResponseException) candidate).getResponseHeaders().getFirst(RETRY_AFTER);

            if (retryAfter != null) {
                return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Long.parseLong(retryAfter));
            }
        }

        return null;
    }
}
