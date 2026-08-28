package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CVmarketJobListingsScraper extends AbstractJobListingsScraper {

    private static final String PROVIDER_NAME = "cvmarket";

    private final CVmarketHtmlParser parser;

    public CVmarketJobListingsScraper(ScraperProperties properties,
                                      CVmarketHtmlParser parser) {

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

        int startOffset = (page - 1) * 30;

        String currentUrl = getTargetUrl();
        return currentUrl + (currentUrl.contains("?") ? "&" : "?") + "start=" + startOffset;
    }

    @Override
    public List<JobListingsDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }

}