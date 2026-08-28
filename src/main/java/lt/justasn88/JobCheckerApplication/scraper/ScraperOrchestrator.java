package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import lt.justasn88.JobCheckerApplication.service.JobListingsNotificationManager;
import lt.justasn88.JobCheckerApplication.service.JobListingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class ScraperOrchestrator implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperOrchestrator.class);

    private final List<AbstractJobListingsScraper> scrapers;
    private final JobListingsService jobListingsService;
    private final JobListingsNotificationManager jobListingsNotificationManager;

    public ScraperOrchestrator (List<AbstractJobListingsScraper> scrapers,
                                JobListingsService jobListingsService,
                                JobListingsNotificationManager jobListingsNotificationManager) {
        this.scrapers = scrapers;
        this.jobListingsService = jobListingsService;
        this.jobListingsNotificationManager = jobListingsNotificationManager;
    }

    @Override
    public void run(String... args) throws Exception {
        int randomDelaySeconds = new Random().nextInt(31) + 15;
        LOGGER.info("Cloud scheduler triggered the application. Waiting {} seconds before scraping to simulate human behavior...", randomDelaySeconds);

        Thread.sleep(randomDelaySeconds * 1000L);

        for (AbstractJobListingsScraper scraper : scrapers) {
            executeScrape(scraper);
        }

        LOGGER.info("All scraping tasks completed successfully. Shutting down the container.");
        System.exit(0);
    }

    private void executeScrape(AbstractJobListingsScraper scraper) {
        try {
            LOGGER.info("Starting scrape for: {}", scraper.getScraperName());
            List<JobListingsDTO> jobs = scraper.performScrape();

            jobListingsService.processJobsListings(jobs, scraper.getScraperName());
            jobListingsService.logExecution(scraper.getScraperName(), "SUCCESS", jobs.size(), null);
        } catch (Exception e) {
            LOGGER.error("Failed to connect to {}", scraper.getScraperName(), e);
            jobListingsNotificationManager.notifyFailure(scraper.getScraperName(), e.getMessage());
            jobListingsService.logExecution(scraper.getScraperName(), "FAILED", 0, e.getMessage());
        }
    }
}