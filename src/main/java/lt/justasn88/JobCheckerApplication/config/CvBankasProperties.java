package lt.justasn88.JobCheckerApplication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;



@Data
@ConfigurationProperties(prefix = "scraper.cvbankas")
public class CvBankasProperties {
    private String url;
    private String cron;
    private String name;
}
