package lt.justasn88.JobCheckerApplication.service;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobListingsNotificationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobListingsNotificationManager.class);

    public void notifyUser(JobListingsDTO job) {
        LOGGER.info("New Job Found: {}\nLink: {}\n---------------------------------------------------",
                job.title(),
                job.url());
    }
    public void notifyFailure(String scraperName, String errorMessage) {
        // TODO: Telegram messenger
        LOGGER.warn("WARNING: Scraper '{}' failed, error: {}", scraperName, errorMessage);
    }
}