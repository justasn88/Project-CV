package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVonlineHtmlParser {

    private static final String BASE_URL = "https://cvonline.lt";

    public List<JobListingsDTO> parseJobs(Document doc) {
        return doc.select("h2.vacancy-item__title a").stream()
                .map(jobLink -> {
                    String title = jobLink.text();
                    String relativeUrl = jobLink.attr("href");

                    String fullUrl = relativeUrl.startsWith("http") ? relativeUrl : BASE_URL + relativeUrl;

                    return new JobListingsDTO(title, fullUrl);
                })
                .filter(dto -> !dto.title().isEmpty())
                .toList();
    }
}