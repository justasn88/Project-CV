package lt.justasn88.JobCheckerApplication.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;


@Getter
@Setter
@Service
@Data
@Configuration
@ConfigurationProperties(prefix = "scraper.cvbankas")
public class CvBankasProperties {
    private String url;
    private String cron;
    private String name;
}
