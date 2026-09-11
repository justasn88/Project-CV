package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.config.ScraperProperties;
import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVbankasJobListingsScraper extends AbstractJobListingsScraper {

    private static final String PROVIDER_NAME = "cvbankas";

    private final CVbankasHtmlParser parser;

    public CVbankasJobListingsScraper(ScraperProperties properties,
                                      CVbankasHtmlParser parser) {

        super(
                properties.providers().get(PROVIDER_NAME),
                properties
        );

        this.parser = parser;
    }

    protected String buildPageUrl(int page) {
        if (page == 1) {
            return getTargetUrl();
        }
        return getTargetUrl() + (getTargetUrl().contains("?") ? "&" : "?") + "page=" + page;
    }

    @Override
    public List<JobListingsDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }
}