package lt.justasn88.JobCheckerApplication.scraper;

import lombok.extern.slf4j.Slf4j;
import lt.justasn88.JobCheckerApplication.model.JobDTO;
import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.TaskScheduler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public abstract class AbstractJobScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobScraper.class);

    protected final JobNotificationManager notificationManager;
    private final TaskScheduler taskScheduler;
    protected final String userAgent;

    protected AbstractJobScraper(JobNotificationManager jobNotificationManager,
                                 TaskScheduler taskScheduler,
                                 String userAgent) {
        this.notificationManager = jobNotificationManager;
        this.taskScheduler = taskScheduler;
        this.userAgent = userAgent;
    }

    protected void applyDelayAndScrape() {
        int randomDelayMs = ThreadLocalRandom.current().nextInt(5_000, 60_000);
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
            LOGGER.error("Failed to connect to {}: {}", getScraperName(), e.getMessage());
        }
    }

    protected abstract String getTargetUrl();

    protected abstract List<JobDTO> extractJobs (Document document);

    protected abstract String getScraperName();
}
