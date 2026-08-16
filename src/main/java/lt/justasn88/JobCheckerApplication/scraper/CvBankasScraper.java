package lt.justasn88.JobCheckerApplication.scraper;

import jakarta.annotation.PostConstruct;
import lt.justasn88.JobCheckerApplication.config.CvBankasProperties;
import lt.justasn88.JobCheckerApplication.model.JobDTO;

import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class CvBankasScraper extends AbstractJobScraper{

    private static final Logger LOGGER = LoggerFactory.getLogger(CvBankasScraper.class);

    private final CvBankasProperties properties;
    private final CVbankasHtmlParser parser;

    public CvBankasScraper(CvBankasProperties properties,
                           CVbankasHtmlParser parser,
                           JobNotificationManager notificationManager,
                           TaskScheduler taskScheduler,
                           @Value("${scraper.user-agent}") String userAgent,
                           @Value("${scraper.delay.min}") int minDelay,
                           @Value("${scraper.delay.max}") int maxDelay) {

        super(notificationManager, taskScheduler, userAgent, minDelay, maxDelay);

        this.properties = properties;
        this.parser = parser;
    }

    @PostConstruct
    public void initSchedule() {
        registerCronSchedule(properties.getCron());
    }

    @Override
    public String getTargetUrl() {
        return properties.getUrl();
    }

    @Override
    public List<JobDTO> extractJobs(Document document) {
        return parser.parseJobs(document);
    }

    @Override
    public String getScraperName() {
        return properties.getName();
    }

}
