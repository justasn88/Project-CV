package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVmarketHtmlParser {

    private static final String BASE_URL = "https://www.cvmarket.lt";

    public List<JobListingsDTO> parseJobs(Document doc) {
        return doc.select("a.jobad-url").stream()
                .map(job -> {
                    String title = job.select("div.font-extrabold").text();

                    String relativeUrl = job.attr("href");
                    String fullUrl = relativeUrl.startsWith("http") ? relativeUrl : BASE_URL + relativeUrl;

                    return new JobListingsDTO(title, fullUrl);
                })
                .filter(dto -> !dto.title().isEmpty())
                .toList();
    }
}