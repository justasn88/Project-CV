package lt.justasn88.JobCheckerApplication.service;

import lt.justasn88.JobCheckerApplication.model.JobDTO;
import lt.justasn88.JobCheckerApplication.model.JobEntity;
import lt.justasn88.JobCheckerApplication.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobNotificationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobNotificationManager.class);

    private final JobRepository jobRepository;

    public JobNotificationManager(JobRepository jobRepository){
        this.jobRepository = jobRepository;
    }

    public void processJobs(List<JobDTO> foundJobs, String scraperName) {
        for (JobDTO jobDTO : foundJobs) {
            if (!jobRepository.existsByUrl(jobDTO.url())) {
                JobEntity newJob = new JobEntity();

                newJob.setTitle(jobDTO.title());
                newJob.setUrl(jobDTO.url());
                newJob.setScraperName(scraperName);
                jobRepository.save(newJob);

                notifyUser(jobDTO);
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