package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class CvBankasJobListingsScraper extends AbstractJobListingsScraper {

    private static final String PROVIDER_NAME = "cvbankas";

    private final CVbankasHtmlParser parser;

    public CvBankasJobListingsScraper(ScraperProperties properties,
                                      CVbankasHtmlParser parser) {

        super(
                properties.providers().get(PROVIDER_NAME),
                properties.userAgent()
                );

        this.parser = parser;
    }

    @Override
    public List<JobListingsDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }

}
