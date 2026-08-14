package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.CvBankasProperties;
import lt.justasn88.JobCheckerApplication.model.JobDTO;

import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;


@Service
public class CvBankasScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CvBankasScraper.class);

    private final CvBankasProperties properties;
    private final CVbankasHtmlParser parser;
    private final JobNotificationManager notificationManager;
    private final TaskScheduler taskScheduler;

    public CvBankasScraper(CvBankasProperties properties,
                           CVbankasHtmlParser parser,
                           JobNotificationManager notificationManager,
                           TaskScheduler taskScheduler) {
        this.properties = properties;
        this.parser = parser;
        this.notificationManager = notificationManager;
        this.taskScheduler = taskScheduler;
    }

    @Scheduled(cron = "${scraper.cvbankas.cron}")
    public void scheduleScrape() {
        int randomDelayMs = new Random().nextInt(60000);
        Instant executionTime = Instant.now().plusMillis(randomDelayMs);
        taskScheduler.schedule(this::perfomScrape, executionTime);
    }

    private void perfomScrape() {
        try {
            Document doc = Jsoup.connect(properties.getUrl())
                    .userAgent(properties.getUserAgent())
                    .get();

            List<JobDTO> jobs = parser.parseJobs(doc);
            notificationManager.processJobs(jobs);
        } catch (IOException e) {
            LOGGER.error("Failed to connect to CVbankas: {}", e.getMessage());
        }
    }
}
