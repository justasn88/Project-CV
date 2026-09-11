package lt.justasn88.jobcheckerapplication.scraper;

import lombok.Getter;
import lt.justasn88.jobcheckerapplication.config.ScraperProperties;
import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public abstract class AbstractJobListingsScraper implements JobListingsScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobListingsScraper.class);

    @Getter
    private final String targetUrl;

    @Getter
    private final String scraperName;

    private final String userAgent;
    private final long requestDelayMs;
    private final Map<String, String> headers;

    protected AbstractJobListingsScraper(ScraperProperties.Provider providerConfig, ScraperProperties scraperProperties){
        this.targetUrl = providerConfig.url();
        this.scraperName = providerConfig.name();
        this.userAgent = scraperProperties.userAgent();
        this.headers = scraperProperties.headers();
        this.requestDelayMs = scraperProperties.requestDelayMs();
    }


    public List<JobListingsDTO> performScrape(int page) throws IOException {
        String currentUrl = buildPageUrl(page);
        LOGGER.info("Searching for jobs: {} page: {}", scraperName, page);

        org.jsoup.Connection connection = Jsoup.connect(currentUrl)
                .userAgent(this.userAgent)
                .timeout(30000)
                .ignoreHttpErrors(true);

        if (this.headers != null && !this.headers.isEmpty()) {
            connection.headers(this.headers);
        }

        org.jsoup.Connection.Response response = connection.execute();

        if (response.statusCode() == 404) {
            LOGGER.info("Page not found (404). Assuming end of pagination.");
            return List.of();
        }
        String decodedCurrentUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8);
        String decodedResponseUrl = URLDecoder.decode(response.url().toString(), StandardCharsets.UTF_8);

        if (!decodedResponseUrl.equals(decodedCurrentUrl)) {
            LOGGER.info("Redirect detected from {} to {}. End of pagination.", currentUrl, response.url());
            return List.of();
        }

        return extractJobListings(response.parse());
    }


    protected abstract String buildPageUrl(int page);

    public abstract List<JobListingsDTO> extractJobListings (Document document);

}
