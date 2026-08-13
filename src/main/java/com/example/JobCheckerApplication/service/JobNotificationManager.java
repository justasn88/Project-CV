package com.example.JobCheckerApplication.service;

import com.example.JobCheckerApplication.model.JobDto;
import com.example.JobCheckerApplication.scraper.JobScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobNotificationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobNotificationManager.class);
    private final Set<String> seenJobs = new HashSet<>();

    public void processJobs(List<JobDto> foundJobs) {
        for (JobDto job : foundJobs) {
            if (!seenJobs.contains(job.url())) {
                seenJobs.add(job.url());
                notifyUser(job);
            }
        }
    }

    private void notifyUser(JobDto job) {
        LOGGER.info("🚨 New Job Found: {}", job.title());
        LOGGER.info("🔗 Link: {}", job.url());
        LOGGER.info("---------------------------------------------------");
    }
}