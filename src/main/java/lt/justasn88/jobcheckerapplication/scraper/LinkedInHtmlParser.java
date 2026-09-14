package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LinkedInHtmlParser {

    public List<JobListingsDTO> parseJobs(Document doc) {
        List<JobListingsDTO> jobsList = new ArrayList<>();
        Elements jobElements = doc.select("ul.jobs-search__results-list > li");

        for (Element el : jobElements) {
            String title = el.select("h3.base-search-card__title").text().trim();
            String jobUrl = el.select("a.base-card__full-link").attr("href");

            if (jobUrl.contains("?")) {
                jobUrl = jobUrl.substring(0, jobUrl.indexOf("?"));
            }

            if (!title.isEmpty() && !jobUrl.isEmpty()) {
                jobsList.add(new JobListingsDTO(title, jobUrl));
            }
        }
        return jobsList;
    }
}