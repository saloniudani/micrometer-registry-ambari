package com.example.metrics.micrometerregistryambari.implementation;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.metrics.export.ambari")
public class AmbariMetricProperties extends StepRegistryProperties {

    private String metricCollectorUrl;
    private String hostname;
    private boolean skipSslValidation = false;
    private String clientCert;
    private String clientCertPassword;
    private String clientCertKeyPassword;
    private String clientTrustStore;
    private String clientTrustStorePassword;

    public String getMetricCollectorUrl() {
        return metricCollectorUrl;
    }

    public void setMetricCollectorUrl(String metricCollectorUrl) {
        this.metricCollectorUrl = metricCollectorUrl;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isSkipSslValidation() {
        return skipSslValidation;
    }

    public void setSkipSslValidation(boolean skipSslValidation) {
        this.skipSslValidation = skipSslValidation;
    }

    public String getClientCert() {
        return clientCert;
    }

    public void setClientCert(String clientCert) {
        this.clientCert = clientCert;
    }

    public String getClientCertPassword() {
        return clientCertPassword;
    }

    public void setClientCertPassword(String clientCertPassword) {
        this.clientCertPassword = clientCertPassword;
    }

    public String getClientCertKeyPassword() {
        return clientCertKeyPassword;
    }

    public void setClientCertKeyPassword(String clientCertKeyPassword) {
        this.clientCertKeyPassword = clientCertKeyPassword;
    }

    public String getClientTrustStore() {
        return clientTrustStore;
    }

    public void setClientTrustStore(String clientTrustStore) {
        this.clientTrustStore = clientTrustStore;
    }

    public String getClientTrustStorePassword() {
        return clientTrustStorePassword;
    }

    public void setClientTrustStorePassword(String clientTrustStorePassword) {
        this.clientTrustStorePassword = clientTrustStorePassword;
    }
}
