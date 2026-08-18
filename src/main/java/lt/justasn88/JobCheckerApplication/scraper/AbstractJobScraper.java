package lt.justasn88.JobCheckerApplication.scraper;

import lombok.Getter;
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

    @Getter
    private final String targetUrl;

    @Getter
    private final String scraperName;

    private final JobNotificationManager notificationManager;
    private final TaskScheduler taskScheduler;
    private final ScraperProperties scraperProperties;

    protected AbstractJobScraper(JobNotificationManager jobNotificationManager,
                                 TaskScheduler taskScheduler,
                                 ScraperProperties scraperProperties,
                                 String targetUrl,
                                 String scraperName) {
        this.notificationManager = jobNotificationManager;
        this.taskScheduler = taskScheduler;
        this.scraperProperties = scraperProperties;
        this.targetUrl = targetUrl;
        this.scraperName = scraperName;
    }

    protected void registerCronSchedule(String cronExpression) {
        LOGGER.info("Registering {} planner with graphic: {}", getScraperName(), cronExpression);

        taskScheduler.schedule(
                this::scheduleNext,
                new CronTrigger(cronExpression)
        );
    }

    public void scheduleNext() {
        Instant executionTime = calculateNextExecutionTime();
        taskScheduler.schedule(this::executeScrape, executionTime);
    }

    protected Instant calculateNextExecutionTime() {
        int minDelay = scraperProperties.getDelay().getMin();
        int maxDelay = scraperProperties.getDelay().getMax();

        int randomDelayMs = (minDelay >= maxDelay)
                ? minDelay
                : ThreadLocalRandom.current().nextInt(minDelay, maxDelay);

        return Instant.now().plusMillis(randomDelayMs);
    }

    private void executeScrape() {
        try {
            Document doc = Jsoup.connect(getTargetUrl())
                    .userAgent(this.scraperProperties.getUserAgent())
                    .get();

            List<JobDTO> jobs = extractJobListings(doc);
            notificationManager.processJobs(jobs, getScraperName());

        } catch (IOException e) {
            LOGGER.error("Failed to connect to {}", getScraperName(), e);

            notificationManager.notifyFailure(getScraperName(), e.getMessage());
        }
    }

    public abstract List<JobDTO> extractJobListings (Document document);

}
