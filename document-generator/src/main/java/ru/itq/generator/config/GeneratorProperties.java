package ru.itq.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    private String serviceUrl = "http://localhost:8080";
    private int n = 100;

    public String getServiceUrl() { return serviceUrl; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl; }

    public int getN() { return n; }
    public void setN(int n) { this.n = n; }
}
