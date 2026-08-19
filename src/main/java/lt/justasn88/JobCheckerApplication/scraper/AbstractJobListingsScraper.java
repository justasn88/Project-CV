package lt.justasn88.JobCheckerApplication.scraper;

import lombok.Getter;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import lt.justasn88.JobCheckerApplication.service.JobListingsService;
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

public abstract class AbstractJobListingsScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobListingsScraper.class);

    private final JobListingsService jobListingsService;
    private final JobNotificationManager notificationManager;
    private final TaskScheduler taskScheduler;
    private final String userAgent;
    private final ScraperProperties.Provider providerConfig;

    @Getter
    private final String targetUrl;

    @Getter
    private final String scraperName;


    protected AbstractJobListingsScraper(JobNotificationManager jobNotificationManager,
                                         TaskScheduler taskScheduler,
                                         ScraperProperties.Provider providerConfig,
                                         String userAgent,
                                         JobListingsService jobListingsService) {
        this.notificationManager = jobNotificationManager;
        this.taskScheduler = taskScheduler;
        this.providerConfig = providerConfig;
        this.userAgent = userAgent;
        this.jobListingsService = jobListingsService;

        this.targetUrl = providerConfig.url();
        this.scraperName = providerConfig.name();
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
        int minDelay = providerConfig.delay().min();
        int maxDelay = providerConfig.delay().max();

        int randomDelayMs = (minDelay >= maxDelay)
                ? minDelay
                : ThreadLocalRandom.current().nextInt(minDelay, maxDelay);

        return Instant.now().plusMillis(randomDelayMs);
    }

    private void executeScrape() {
        try {
            Document doc = Jsoup.connect(getTargetUrl())
                    .userAgent(this.userAgent)
                    .get();

            List<JobListingsDTO> jobs = extractJobListings(doc);
            jobListingsService.processJobsListings(jobs, getScraperName());

            jobListingsService.logExecution(getScraperName(), "SUCCESS", jobs.size(), null);
        } catch (IOException e) {
            LOGGER.error("Failed to connect to {}", getScraperName(), e);

            notificationManager.notifyFailure(getScraperName(), e.getMessage());

            jobListingsService.logExecution(getScraperName(),"Failed", 0, e.getMessage());
        }
    }

    public abstract List<JobListingsDTO> extractJobListings (Document document);

}
