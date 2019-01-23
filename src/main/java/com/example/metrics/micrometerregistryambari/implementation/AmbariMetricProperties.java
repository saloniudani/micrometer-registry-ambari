package com.example.metrics.micrometerregistryambari.implementation;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

@ConfigurationProperties(prefix = "management.metrics.export.ambari")
public class AmbariMetricProperties extends StepRegistryProperties {

    private String metricCollectorUrl;
    private String hostname;
    private String clientCert;
    private String clientCertPassword;
    private String clientCertKeyPassword;
    private String clientTrustStore;
    private String clientTrustStorePassword;
    private String[] tagsAsPrefix;

    public String getMetricCollectorUrl() {
        return metricCollectorUrl;
    }

    public void setMetricCollectorUrl(String metricCollectorUrl) {
        this.metricCollectorUrl = metricCollectorUrl;
    }

    public String getHostname() {
        if (this.hostname == null) {
            String hostName = null;
            try {
                hostName = InetAddress.getLocalHost().getHostName();
                // If not FQDN , call DNS
                if ((hostName == null) || (!hostName.contains("."))) {
                    hostName = InetAddress.getLocalHost().getCanonicalHostName();
                }
            } catch (UnknownHostException e) {
                System.out.println("Could not identify hostname." + e);
            }
            this.hostname = hostName;
        }
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public String[] getTagsAsPrefix() {
        return tagsAsPrefix;
    }

    public void setTagsAsPrefix(String[] tagsAsPrefix) {
        this.tagsAsPrefix = tagsAsPrefix;
    }
}
