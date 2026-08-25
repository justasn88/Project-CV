package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVbankasHtmlParser {

    public List<JobListingsDTO> parseJobs(Document doc) {
        return doc.select("a.list_a").stream()
                .map(job -> new JobListingsDTO(
                        job.select("h3.list_h3").text(),
                        job.attr("href")
                ))
                .toList();
    }
}
