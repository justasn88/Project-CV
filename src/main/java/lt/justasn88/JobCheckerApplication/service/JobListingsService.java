package lt.justasn88.JobCheckerApplication.service;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import lt.justasn88.JobCheckerApplication.model.JobListingsEntity;
import lt.justasn88.JobCheckerApplication.model.ScraperExecutionEntity;
import lt.justasn88.JobCheckerApplication.repository.JobListingsRepository;
import lt.justasn88.JobCheckerApplication.repository.ScraperExecutionRepository;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.List;

@Service
public class JobListingsService {

    private final JobListingsRepository jobListingsRepository;
    private final ScraperExecutionRepository executionRepository;

    private final JobNotificationManager notificationManager;

    public JobListingsService(JobListingsRepository jobListingsRepository,
                              JobNotificationManager notificationManager,
                              ScraperExecutionRepository executionRepository) {
        this.jobListingsRepository = jobListingsRepository;
        this.notificationManager = notificationManager;
        this.executionRepository = executionRepository;
    }


    public void processJobsListings(List<JobListingsDTO> foundJobs, String scraperName) {
        for (JobListingsDTO jobListingsDTO : foundJobs) {
            if (!jobListingsRepository.existsByUrl(jobListingsDTO.url())) {
                JobListingsEntity newJob = new JobListingsEntity();

                newJob.setTitle(jobListingsDTO.title());
                newJob.setUrl(jobListingsDTO.url());
                newJob.setScraperName(scraperName);
                jobListingsRepository.save(newJob);

                notificationManager.notifyUser(jobListingsDTO);
            }
        }
    }

    public void logExecution(String scraperName, String status, int jobsFound, String errorMessage) {
        ScraperExecutionEntity execution = new ScraperExecutionEntity();
        execution.setScraperName(scraperName);
        execution.setStatus(status);
        execution.setJobListingsFound(jobsFound);
        execution.setErrorMessage(errorMessage);

        executionRepository.save(execution);
    }

}
