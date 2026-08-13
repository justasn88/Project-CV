package com.example.JobCheckerApplication.scraper;

import com.example.JobCheckerApplication.model.JobDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParser {

    public List<JobDto> parseJobs(Document doc) {
        List<JobDto> parsedJobs = new ArrayList<>();
        Elements jobListings = doc.select("a.list_a");

        for (Element job : jobListings) {
            String jobURL = job.attr("href");
            String title = job.select("h3.list_h3").text();

            parsedJobs.add(new JobDto(title, jobURL));
        }

        return parsedJobs;
    }
}
