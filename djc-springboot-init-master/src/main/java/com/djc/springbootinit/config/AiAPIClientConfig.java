package com.djc.springbootinit.config;

import com.djc.springbootinit.client.AiAPIClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@ConfigurationProperties("aipai.client")
@ComponentScan
public class AiAPIClientConfig {
    private String apiKey;
    private String modelId;

    @Bean
    public AiAPIClient aiAPIClient() {
        return new AiAPIClient(apiKey, modelId);
    }

    public AiAPIClientConfig() {
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiAPIClientConfig that = (AiAPIClientConfig) o;
        return Objects.equals(apiKey, that.apiKey) && Objects.equals(modelId, that.modelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, modelId);
    }

    @Override
    public String toString() {
        return "AiAPIClientConfig{" +
                "apiKey='" + apiKey + '\'' +
                ", modelId='" + modelId + '\'' +
                '}';
    }
}
