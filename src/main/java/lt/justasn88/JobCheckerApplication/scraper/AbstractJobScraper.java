package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobDTO;
import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractJobScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobScraper.class);

    private final JobNotificationManager notificationManager;
    private final TaskScheduler taskScheduler;
    private final ScraperProperties scraperProperties;

    protected AbstractJobScraper(JobNotificationManager jobNotificationManager,
                                 TaskScheduler taskScheduler,
                                 ScraperProperties scraperProperties) {
        this.notificationManager = jobNotificationManager;
        this.taskScheduler = taskScheduler;
        this.scraperProperties = scraperProperties;
    }

    protected void registerCronSchedule(String cronExpression) {
        LOGGER.info("Registering {} planner with graphic: {}", getScraperName(), cronExpression);

        taskScheduler.schedule(
                this::scheduleNext,
                new CronTrigger(cronExpression)
        );
    }

    public void scheduleNext() {
        int randomDelayMs = ThreadLocalRandom.current().nextInt(scraperProperties.getDelay().getMin(), scraperProperties.getDelay().getMin());
        Instant executionTime = Instant.now().plusMillis(randomDelayMs);

        taskScheduler.schedule(this::executeScrape, executionTime);
    }

    private void executeScrape() {
        try {
            Document doc = Jsoup.connect(getTargetUrl())
                    .userAgent(this.scraperProperties.getUserAgent())
                    .get();

            List<JobDTO> jobs = extractJobs(doc);
            notificationManager.processJobs(jobs);

        } catch (IOException e) {
            LOGGER.error("Failed to connect to {}", getScraperName(), e);

            notificationManager.notifyFailure(getScraperName(), e.getMessage());
        }
    }

    public abstract String getTargetUrl();

    public abstract List<JobDTO> extractJobs (Document document);

    public abstract String getScraperName();
}
