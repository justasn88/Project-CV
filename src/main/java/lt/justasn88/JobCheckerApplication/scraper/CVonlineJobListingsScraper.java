package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVonlineJobListingsScraper extends AbstractJobListingsScraper {

    private static final String PROVIDER_NAME = "cvonline";

    private final CVonlineHtmlParser parser;

    public CVonlineJobListingsScraper(ScraperProperties properties,
                                      CVonlineHtmlParser parser) {

        super(
                properties.providers().get(PROVIDER_NAME),
                properties
        );

        this.parser = parser;
    }

    @Override
    public List<JobListingsDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }

    protected String buildPageUrl(int page) {
        if (page == 1) return getTargetUrl();
        int offset = (page - 1) * 20;
        return getTargetUrl() + (getTargetUrl().contains("?") ? "&" : "?") + "offset=" + offset;
    }
}