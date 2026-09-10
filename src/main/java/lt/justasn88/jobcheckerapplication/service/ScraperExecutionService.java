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
    private final ScraperDispatchService dispatchService;

    public ScraperExecutionService(List<JobListingsScraper> scrapers,
                                   JobListingsService jobListingsService,
                                   JobListingsNotificationManager jobListingsNotificationManager,
                                   ScraperDispatchService dispatchService) {
        this.scrapers = scrapers;
        this.jobListingsService = jobListingsService;
        this.jobListingsNotificationManager = jobListingsNotificationManager;
        this.dispatchService = dispatchService;
    }

    public void executeTargetScrapers(String targetScraper, int targetPage, String host) {
        LOGGER.info("Executing scrape logic for: {} (Page: {})", targetScraper, targetPage);
        for (JobListingsScraper scraper : scrapers) {
            if ("ALL".equalsIgnoreCase(targetScraper) || targetScraper.equalsIgnoreCase(scraper.getScraperName())) {
                executeSingleScraper(scraper, targetPage, host);
            }
        }
    }

    private void executeSingleScraper(JobListingsScraper scraper, int targetPage, String host) {
        try {
            LOGGER.info("Starting scrape logic for: {} (Page: {})", scraper.getScraperName(), targetPage);

            List<JobListingsDTO> jobs = scraper.performScrape(targetPage);

            jobListingsService.processJobsListings(jobs, scraper.getScraperName());
            jobListingsService.logExecution(scraper.getScraperName(), "SUCCESS", jobs.size(), null);
        } catch (Exception e) {
            LOGGER.error("Failed to connect to {}", scraper.getScraperName(), e);

            String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();

            boolean isTimeout = errorMessage.toLowerCase().contains("timeout") ||
                    e.getClass().getSimpleName().toLowerCase().contains("timeout");

            if (!isTimeout) {
                jobListingsNotificationManager.notifyFailure(scraper.getScraperName(), errorMessage);
            } else {
                LOGGER.info("Ignoruojamas timeout pranešimas į Telegram scraper'iui: {}", scraper.getScraperName());
            }
            jobListingsService.logExecution(scraper.getScraperName(), "FAILED", 0, errorMessage);
        }
    }
}