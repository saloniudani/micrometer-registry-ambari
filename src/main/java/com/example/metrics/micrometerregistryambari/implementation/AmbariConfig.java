package com.example.metrics.micrometerregistryambari.implementation;

import io.micrometer.core.instrument.step.StepRegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class AmbariConfig implements StepRegistryConfig {

    @Value("${app.ambari.metric.app.id:helix}")
    public String appId;

    @Value("${app.ambari.metric.monitoring.host}")
    public String ambariMonitoringHost;

    @Value("${app.ambari.metric.name.prefix:app.helix}")
    public String prefix;

    public String hostName;

    @PostConstruct
    public void setup(){
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            // If not FQDN , call DNS
            if ((hostName == null) || (!hostName.contains("."))) {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            }
        } catch (UnknownHostException e) {
            log.error("Could not identify hostname.", e);
        }
    }


    @Override
    public String prefix() {
        return prefix;
    }

    @Override
    public String get(String key) {
        return null;
    }
}
