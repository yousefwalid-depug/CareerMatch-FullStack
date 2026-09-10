package com.careermatch.careermatch_backend.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "careermatch.ai.candidate-extraction")
public class CandidateExtractionProperties {
    private String provider = "auto";
    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4.1-mini";
    private double temperature = 0.0;
    private int maxOutputTokens = 2500;
    private int timeoutSeconds = 12;
    private int maxInputCharacters = 60000;

    public boolean useOpenAi() {
        if ("fallback".equalsIgnoreCase(provider)) return false;
        return !apiKey.isBlank() && ("auto".equalsIgnoreCase(provider) || "openai".equalsIgnoreCase(provider));
    }

    public String responsesUrl() {
        return baseUrl.replaceAll("/+$", "") + "/responses";
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider == null ? "auto" : provider.trim(); }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey == null ? "" : apiKey.trim(); }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxOutputTokens() { return maxOutputTokens; }
    public void setMaxOutputTokens(int maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public int getMaxInputCharacters() { return maxInputCharacters; }
    public void setMaxInputCharacters(int maxInputCharacters) { this.maxInputCharacters = maxInputCharacters; }
}
