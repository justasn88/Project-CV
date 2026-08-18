package lt.justasn88.JobCheckerApplication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "scraper")
public class ScraperProperties {

    private String userAgent;
    private Map<String, Provider> providers;

    @Data
    public static class Delay {
        private int min;
        private int max;
    }

    @Data
    public static class Provider {
        private String url;
        private String cron;
        private String name;
        private Delay delay;
    }
}