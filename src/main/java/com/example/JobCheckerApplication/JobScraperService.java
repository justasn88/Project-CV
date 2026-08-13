package com.example.JobCheckerApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


@Service
public class JobScraperService {
    private final Set<String> seenJobs = new HashSet<>();

    private final String searchURL = "https://www.cvbankas.lt/?city=Vilnius%2CKaunas%2CKlaipeda&keyw=Junior+Java";

    @Scheduled(fixedDelay = 600_000)
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
            System.err.println("Failed to scrape jobs: " + e.getMessage());
        }
    }
    private void notifyUser(String title, String url) {
        System.out.println("🚨 New Job Found: " + title);
        System.out.println("🔗 Link: " + url);
        System.out.println("---------------------------------------------------");
    }
}

