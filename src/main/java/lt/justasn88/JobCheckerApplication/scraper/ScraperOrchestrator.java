package lt.justasn88.JobCheckerApplication.scraper;

import jakarta.annotation.PostConstruct;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import lt.justasn88.JobCheckerApplication.service.JobListingsNotificationManager;
import lt.justasn88.JobCheckerApplication.service.JobListingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ScraperOrchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperOrchestrator.class);

    private final List<AbstractJobListingsScraper> scrapers;
    private final ScraperProperties properties;
    private final TaskScheduler taskScheduler;
    private final JobListingsService jobListingsService;
    private final JobListingsNotificationManager jobListingsNotificationManager;

    public ScraperOrchestrator (List<AbstractJobListingsScraper> scrapers,
                                ScraperProperties properties,
                                TaskScheduler taskScheduler,
                                JobListingsService jobListingsService,
                                JobListingsNotificationManager jobListingsNotificationManager) {
        this.scrapers = scrapers;
        this.properties = properties;
        this.taskScheduler = taskScheduler;
        this.jobListingsService = jobListingsService;
        this.jobListingsNotificationManager = jobListingsNotificationManager;
    }

    @PostConstruct
    public void initSchedules(){
        for (AbstractJobListingsScraper scraper : scrapers) {
            String configKey = scraper.getScraperName().toLowerCase();
            ScraperProperties.Provider config = properties.providers().get(configKey);

            if (config != null){
                LOGGER.info("Registering {} planner with schedule: {}", scraper.getScraperName(), config.cron());
                taskScheduler.schedule(
                        () -> scheduleNext(scraper, config),
                        new CronTrigger(config.cron())
                );
            }
        }
    }

    private void scheduleNext(AbstractJobListingsScraper scraper, ScraperProperties.Provider config) {
        int minDelay = config.delay().min();
        int maxDelay = config.delay().max();
        long randomDelayMs = calculateRandomDelay(minDelay, maxDelay);

        taskScheduler.schedule(() -> executeScrape(scraper), Instant.now().plusMillis(randomDelayMs));
    }

    long calculateRandomDelay(int minDelay, int maxDelay) {
        return (minDelay >= maxDelay)
                ? minDelay
                : ThreadLocalRandom.current().nextInt(minDelay,maxDelay);
    }

    private void executeScrape(AbstractJobListingsScraper scraper) {
        try {
            List<JobListingsDTO> jobs = scraper.performScrape();

            jobListingsService.processJobsListings(jobs, scraper.getScraperName());
        } catch (Exception e) {
            LOGGER.error("Failed to connect to {}", scraper.getScraperName(), e);
            jobListingsNotificationManager.notifyFailure(scraper.getScraperName(), e.getMessage());
        }
    }
}
