package com.example.metrics.micrometerregistryambari.implementation;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Slf4j
public class SslVerificationHttpRequestFactory {

    public static class SSLRestTemplateBuilder {

        public static RestTemplate getRestTemplate(String metricCollectorUrl,String clientCert,String clientCertPassword,String clientCertKeyPassword,String clientTrustStore,String clientTrustStorePassword) {
            final RestTemplateBuilder restTemplateBuilder;
            if (metricCollectorUrl.startsWith("https")) {
                try {
                    SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
                    if (clientTrustStore != null) {
                        File truststoreFile = ResourceUtils.getFile(clientTrustStore);
                        sslContextBuilder
                                .setKeyStoreType("PKCS12")
                                .loadTrustMaterial(truststoreFile, clientTrustStorePassword.toCharArray());
                    } else {
                        log.info("No custom truststore configured. Using default JAVA truststore.");
                    }
                    try {
                        if (clientCert != null) {
                            sslContextBuilder
                                    .setKeyStoreType("PKCS12")
                                    .loadKeyMaterial(ResourceUtils.getFile(clientCert), clientCertPassword.toCharArray()
                                            , clientCertKeyPassword.toCharArray());
                            log.info("2 way SSL is enabled.");
                        } else {
                            log.info("Client certificate is not configured so only one-way SSL is enabled.");
                        }
                    } catch (FileNotFoundException e) {
                        log.info("Client certificate {} not found so only one-way SSL is enabled using {}."
                                , clientCert, clientTrustStore);
                    }
                    HttpClient client = HttpClients.custom().setSSLContext(sslContextBuilder.build()).build();

                    restTemplateBuilder = new RestTemplateBuilder()
                            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client));
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException("Missing/invalid truststore file " + clientTrustStore +
                            ". Either place the truststore file or disable SSL by changing service URL."
                            + NestedExceptionUtils.getMostSpecificCause(e).toString());
                } catch (IOException | CertificateException | UnrecoverableKeyException
                        | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                    throw new IllegalStateException("Security Configuration issue "
                            + NestedExceptionUtils.getMostSpecificCause(e).toString());
                }
            } else {
                log.info("SSL is not used in metric collector URL as it is not 'https://'.");
                restTemplateBuilder = new RestTemplateBuilder();
            }
            return restTemplateBuilder.interceptors(
                    (ClientHttpRequestInterceptor) (request, body, execution) -> execution.execute(request, body))
                    .setConnectTimeout(10000)
                    .setReadTimeout(5000)
                    .build();
        }
    }


}
