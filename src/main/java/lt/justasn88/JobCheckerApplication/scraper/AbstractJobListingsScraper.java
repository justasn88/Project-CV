package lt.justasn88.JobCheckerApplication.scraper;

import lombok.Getter;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractJobListingsScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperOrchestrator.class);

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


    public List<JobListingsDTO> performScrape() throws IOException {
        java.util.Set<JobListingsDTO> allJobs = new java.util.LinkedHashSet<>();
        final int MAX_PAGES = 5;

        for (int page = 1; page <= MAX_PAGES; page++) {
            List<JobListingsDTO> jobsOnPage = fetchJobsFromPage(page);

            if (jobsOnPage.isEmpty()) {
                LOGGER.info("End of pagination (empty page).");
                break;
            }

            int sizeBefore = allJobs.size();
            allJobs.addAll(jobsOnPage);

            if (allJobs.size() == sizeBefore) {
                LOGGER.info("Job count did not increase (total remains {}). Page {} returned duplicate jobs. Stopping.", allJobs.size(), page);
                break;
            }

            pauseToAvoidBan();
        }

        return new java.util.ArrayList<>(allJobs);
    }

    private List<JobListingsDTO> fetchJobsFromPage(int page) throws IOException {
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
            LOGGER.info("Page not found (404) for {}. Assuming end of pagination.", currentUrl);
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

    private void pauseToAvoidBan() {
        try {
            Thread.sleep(this.requestDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Scraping forcefully terminated.");
        }
    }

    protected abstract String buildPageUrl(int page);

    public abstract List<JobListingsDTO> extractJobListings (Document document);

}
