package lt.justasn88.JobCheckerApplication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "scraper")
public class ScraperProperties {

    private String userAgent;
    private Delay delay;

    @Data
    public static class Delay {
        private int min;
        private int max;
    }

}