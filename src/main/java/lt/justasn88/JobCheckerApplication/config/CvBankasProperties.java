package lt.justasn88.JobCheckerApplication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;



@Data
@Configuration
@ConfigurationProperties(prefix = "scraper.cvbankas")
public class CvBankasProperties {
    private String url;
    private String cron;
    private String name;
}
