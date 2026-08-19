package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CVbankasHtmlParser {

    public List<JobListingsDTO> parseJobs(Document doc) {
        List<JobListingsDTO> parsedJobs = new ArrayList<>();
        Elements jobListings = doc.select("a.list_a");

        for (Element job : jobListings) {
            String jobURL = job.attr("href");
            String title = job.select("h3.list_h3").text();

            parsedJobs.add(new JobListingsDTO(title, jobURL));
        }

        return parsedJobs;
    }
}
