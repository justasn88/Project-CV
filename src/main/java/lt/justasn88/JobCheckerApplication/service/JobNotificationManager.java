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
        LOGGER.info("🚨 New Job Found: {}\n🔗 Link: {}\n---------------------------------------------------",
                job.title(),
                job.url());
    }
}