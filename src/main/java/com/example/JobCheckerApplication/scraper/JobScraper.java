package com.example.JobCheckerApplication.scraper;

import com.example.JobCheckerApplication.model.JobDto;

import java.util.List;

public interface JobScraper {
    List<JobDto> scrapeJobs();
}
