package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobDTO;

import java.util.List;

public interface JobScraper {
    List<JobDTO> scrapeJobs();
}
