package lt.justasn88.jobcheckerapplication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "scraper")
public record ScraperProperties(
        String userAgent,
        Map<String, String> headers,
        long requestDelayMs,
        Map<String, Provider> providers
) {

    public record Delay(int min, int max) {}

    public record Provider(
            String url,
            String cron,
            String name,
            Delay delay
    ) {}
}