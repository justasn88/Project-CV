package com.example.JobCheckerApplication.scraper;

import com.example.JobCheckerApplication.config.CvBankasProperties;
import com.example.JobCheckerApplication.model.JobDto;

import com.example.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class CvBankasScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CvBankasScraper.class);

    private final CvBankasProperties properties;
    private final HtmlParser parser;
    private final JobNotificationManager notificationManager;

    // Pridedame vadybininką į konstruktorių
    public CvBankasScraper(CvBankasProperties properties,
                           HtmlParser parser,
                           JobNotificationManager notificationManager) {
        this.properties = properties;
        this.parser = parser;
        this.notificationManager = notificationManager;
    }

    @Scheduled(cron = "${scraper.cvbankas.cron}")
    public void scrapeAndNotify() {
        try {
            Document doc = Jsoup.connect(properties.getUrl())
                    .userAgent(properties.getUserAgent())
                    .get();

            List<JobDto> jobs = parser.parseJobs(doc);
            notificationManager.processJobs(jobs);

        } catch (IOException e) {
            LOGGER.error("Failed to connect to CVbankas: {}", e.getMessage());
            throw new RuntimeException("Error:", e);
        }
    }
}
