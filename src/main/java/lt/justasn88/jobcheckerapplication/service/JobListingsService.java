package lt.justasn88.jobcheckerapplication.service;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import lt.justasn88.jobcheckerapplication.model.JobListingsEntity;
import lt.justasn88.jobcheckerapplication.model.ScraperExecutionEntity;
import lt.justasn88.jobcheckerapplication.repository.JobListingsRepository;
import lt.justasn88.jobcheckerapplication.repository.ScraperExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
@Service
public class JobListingsService {

    private final JobListingsRepository jobListingsRepository;
    private final ScraperExecutionRepository executionRepository;

    private final JobListingsNotificationManager notificationManager;

    public JobListingsService(JobListingsRepository jobListingsRepository,
                              JobListingsNotificationManager notificationManager,
                              ScraperExecutionRepository executionRepository) {
        this.jobListingsRepository = jobListingsRepository;
        this.notificationManager = notificationManager;
        this.executionRepository = executionRepository;
    }


    public void processJobsListings(List<JobListingsDTO> foundJobs, String scraperName) {
        if (foundJobs.isEmpty()) return;

        List<String> scrapedUrls = foundJobs.stream().map(JobListingsDTO::url).toList();
        Set<String> existingUrls = jobListingsRepository.findExistingUrls(scrapedUrls);

        List<JobListingsDTO> newJobDTOs = foundJobs.stream()
                .filter(job -> !existingUrls.contains(job.url()))
                .toList();

        if (newJobDTOs.isEmpty()) {
            return;
        }

        List<JobListingsEntity> newJobs = newJobDTOs.stream()
                .map(job -> {
                    JobListingsEntity entity = new JobListingsEntity();
                    entity.setTitle(job.title());
                    entity.setUrl(job.url());
                    entity.setScraperName(scraperName);
                    return entity;
                }).toList();

        jobListingsRepository.saveAll(newJobs);
        notificationManager.notifyUsers(newJobDTOs);
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
