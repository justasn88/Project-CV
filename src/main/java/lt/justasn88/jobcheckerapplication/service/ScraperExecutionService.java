package lt.justasn88.jobcheckerapplication.service;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import lt.justasn88.jobcheckerapplication.scraper.JobListingsScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScraperExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperExecutionService.class);

    private final List<JobListingsScraper> scrapers;
    private final JobListingsService jobListingsService;
    private final JobListingsNotificationManager jobListingsNotificationManager;

    public ScraperExecutionService(List<JobListingsScraper> scrapers,
                                   JobListingsService jobListingsService,
                                   JobListingsNotificationManager jobListingsNotificationManager) {
        this.scrapers = scrapers;
        this.jobListingsService = jobListingsService;
        this.jobListingsNotificationManager = jobListingsNotificationManager;
    }

    public void executeTargetScrapers(String targetScraper) {
        LOGGER.info("Executing actual scrape logic for: {}", targetScraper);
        for (JobListingsScraper scraper : scrapers) {
            if ("ALL".equalsIgnoreCase(targetScraper) || targetScraper.equalsIgnoreCase(scraper.getScraperName())) {
                executeSingleScraper(scraper);
            }
        }
    }

    private void executeSingleScraper(JobListingsScraper scraper) {
        try {
            LOGGER.info("Starting scrape logic for: {}", scraper.getScraperName());
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