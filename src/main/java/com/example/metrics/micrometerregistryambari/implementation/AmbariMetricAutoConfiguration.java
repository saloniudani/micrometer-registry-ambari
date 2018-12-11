package com.example.metrics.micrometerregistryambari.implementation;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

@AutoConfigureBefore({CompositeMeterRegistryAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class})
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@Configuration
@ConditionalOnProperty(name = {"management.metrics.export.ambari.metricCollectorUrl"})
public class AmbariMetricAutoConfiguration {

    @Value("${management.metrics.tags.application:helix-web}")
    public String appName;

    @Bean
    public AmbariConfig ambariConfig(AmbariMetricProperties ambariMetricProperties) {
        return new AmbariMetricPropertiesConfigAdapter(ambariMetricProperties);
    }

    @Bean
    AmbariMetricProperties ambariMetricProperties() {
        return new AmbariMetricProperties();
    }

    @Bean
    @Lazy
    AmbariMetricPublisher ambariMetricPublisher(AmbariMetricProperties ambariMetricProperties) {
        RestTemplate restTemplate = SslVerificationHttpRequestFactory.SSLRestTemplateBuilder
                .getRestTemplate(ambariMetricProperties.getMetricCollectorUrl(), ambariMetricProperties.getClientCert(),
                        ambariMetricProperties.getClientCertPassword(), ambariMetricProperties.getClientCertKeyPassword(),
                        ambariMetricProperties.getClientTrustStore(), ambariMetricProperties.getClientTrustStorePassword());
        return new AmbariMetricPublisher(ambariMetricProperties, restTemplate);
    }

    @Bean
    AmbariMetricHierarchicalNameMapper ambariMetricHierarchicalNameMapper(AmbariConfig ambariConfig) {
        return new AmbariMetricHierarchicalNameMapper(ambariConfig.tagsAsPrefix());
    }

    @Bean
    public AmbariMeterRegistry ambariMeterRegistry(AmbariConfig ambariConfig, AmbariMetricPublisher ambariMetricPublisher, AmbariMetricHierarchicalNameMapper ambariMetricHierarchicalNameMapper) {
        return new AmbariMeterRegistry(ambariConfig, ambariMetricPublisher, ambariMetricHierarchicalNameMapper);
    }

    //TODO: Remove when move to spring boot version 2.1.0
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("app", appName);
    }

}
