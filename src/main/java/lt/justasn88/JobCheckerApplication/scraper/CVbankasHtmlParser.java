package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CVbankasHtmlParser {

    public List<JobDTO> parseJobs(Document doc) {
        List<JobDTO> parsedJobs = new ArrayList<>();
        Elements jobListings = doc.select("a.list_a");

        for (Element job : jobListings) {
            String jobURL = job.attr("href");
            String title = job.select("h3.list_h3").text();

            parsedJobs.add(new JobDTO(title, jobURL));
        }

        return parsedJobs;
    }
}
