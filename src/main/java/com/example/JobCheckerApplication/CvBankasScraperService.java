package com.example.JobCheckerApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;



@Service
public class CvBankasScraperService {
    private final Set<String> seenJobs = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(CvBankasScraperService.class);

    @Value("${scraper.cvbankas.url}")
    private String searchURL;

    @Scheduled(cron = "0 */10 * * * *")
    public void checkForNewJobs() {
        try {
            Document doc = Jsoup.connect(searchURL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Elements jobListings = doc.select("a.list_a");

            for (Element job : jobListings) {
                String jobURL = job.attr("href");
                String title = job.select("h3.list_h3").text();

                if(!seenJobs.contains(jobURL)){
                    seenJobs.add(jobURL);
                    notifyUser(title, jobURL);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Nepavyko prisijungti prie CVBanko", e);
        }
    }
    private void notifyUser(String title, String url) {
        logger.info("🚨 New Job Found: {}", title);
        logger.info("🔗 Link: {}", url);
        logger.info("---------------------------------------------------");
    }
}

