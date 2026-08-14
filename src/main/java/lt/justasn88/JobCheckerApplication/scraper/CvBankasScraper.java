package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.CvBankasProperties;
import lt.justasn88.JobCheckerApplication.model.JobDTO;

import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;


@Service
public class CvBankasScraper extends AbstractJobScraper{

    private static final Logger LOGGER = LoggerFactory.getLogger(CvBankasScraper.class);

    private final CvBankasProperties properties;
    private final CVbankasHtmlParser parser;

    public CvBankasScraper(CvBankasProperties properties,
                           CVbankasHtmlParser parser,
                           JobNotificationManager notificationManager,
                           TaskScheduler taskScheduler,
                           @Value("{$scraper.user-agent}") String userAgent) {

        super(notificationManager, taskScheduler, userAgent);

        this.properties = properties;
        this.parser = parser;
    }

    @Scheduled(cron = "${scraper.cvbankas.cron}")
    public void trigger() {
        applyDelayAndScrape();
    }

    @Override
    protected String getTargetUrl() {
        return properties.getUrl();
    }

    @Override
    protected List<JobDTO> extractJobs(Document document) {
        return parser.parseJobs(document);
    }

    @Override
    protected String getScraperName() {
        return "CVbankas";
    }
}
