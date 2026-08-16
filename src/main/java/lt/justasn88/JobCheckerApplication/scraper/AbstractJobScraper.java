package lt.justasn88.JobCheckerApplication.scraper;

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
    private final String userAgent;

    private final int maxDelay;
    private final int minDelay;

    protected AbstractJobScraper(JobNotificationManager jobNotificationManager,
                                 TaskScheduler taskScheduler,
                                 String userAgent,
                                 int minDelay,
                                 int maxDelay) {
        this.notificationManager = jobNotificationManager;
        this.taskScheduler = taskScheduler;
        this.userAgent = userAgent;
        this.maxDelay = maxDelay;
        this.minDelay = minDelay;
    }

    protected void registerCronSchedule(String cronExpression) {
        LOGGER.info("Registering {} planner with graphic: {}", getScraperName(), cronExpression);

        taskScheduler.schedule(
                this::scheduleNext,
                new CronTrigger(cronExpression)
        );
    }

    public void scheduleNext() {
        int randomDelayMs = ThreadLocalRandom.current().nextInt(minDelay, maxDelay);
        Instant executionTime = Instant.now().plusMillis(randomDelayMs);

        taskScheduler.schedule(this::executeScrape, executionTime);
    }

    private void executeScrape() {
        try {
            Document doc = Jsoup.connect(getTargetUrl())
                    .userAgent(this.userAgent)
                    .get();

            List<JobDTO> jobs = extractJobs(doc);
            notificationManager.processJobs(jobs);

        } catch (IOException e) {
            LOGGER.error("Failed to connect to {}", "${scraper.cvbankas.name}", e);
        }
    }

    public abstract String getTargetUrl();

    public abstract List<JobDTO> extractJobs (Document document);

    public abstract String getScraperName();
}
