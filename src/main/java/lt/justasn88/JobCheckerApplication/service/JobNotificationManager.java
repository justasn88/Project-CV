package lt.justasn88.JobCheckerApplication.service;

import lt.justasn88.JobCheckerApplication.model.JobDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobNotificationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobNotificationManager.class);

    // TODO: Change to database
    private final Set<String> seenJobs = new HashSet<>();

    public void processJobs(List<JobDTO> foundJobs) {
        for (JobDTO job : foundJobs) {
            if (!seenJobs.contains(job.url())) {
                seenJobs.add(job.url());
                notifyUser(job);
            }
        }
    }

    private void notifyUser(JobDTO job) {
        LOGGER.info("New Job Found: {}\nLink: {}\n---------------------------------------------------",
                job.title(),
                job.url());
    }
    public void notifyFailure(String scraperName, String errorMessage) {
        // TODO: Telegram messenger
        LOGGER.warn("WARNING: Scraper '{}' failed, error: {}", scraperName, errorMessage);
    }
}