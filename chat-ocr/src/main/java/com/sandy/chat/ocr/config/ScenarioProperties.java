package com.sandy.chat.ocr.config;
import com.sandy.chat.ocr.model.Scenario;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
@Component
@ConfigurationProperties(prefix = "chat")
public class ScenarioProperties {
    private String storagePath = "data";
    private Map<String, ScenarioConfig> scenarios = new HashMap<>();
    @Data
    public static class ScenarioConfig {
        private String name;
        private String icon;
        private String systemPrompt;
        private List<String> quickActions;
    }
    public ScenarioConfig getScenarioConfig(Scenario scenario) {
        return scenarios.get(scenario.name().toLowerCase());
    }
}