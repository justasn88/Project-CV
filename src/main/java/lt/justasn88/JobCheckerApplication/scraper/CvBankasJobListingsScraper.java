package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class CvBankasJobListingsScraper extends AbstractJobListingsScraper {

    private final CVbankasHtmlParser parser;

    public CvBankasJobListingsScraper(ScraperProperties properties,
                                      CVbankasHtmlParser parser) {

        super(
                properties.providers().get("cvbankas").url(),
                properties.providers().get("cvbankas").name(),
                properties.userAgent()
                );

        this.parser = parser;
    }

    @Override
    public List<JobListingsDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }

}
