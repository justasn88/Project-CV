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


    public List<JobListingsDTO> performScrape() throws IOException {
        java.util.Map<String, JobListingsDTO> allJobs = new java.util.LinkedHashMap<>();
        final int MAX_PAGES = 5;

        for (int page = 1; page <= MAX_PAGES; page++) {
            List<JobListingsDTO> jobsOnPage = fetchJobsFromPage(page);

            if (jobsOnPage.isEmpty()) {
                LOGGER.info("End of pagination (empty page).");
                break;
            }

            int sizeBefore = allJobs.size();

            for (JobListingsDTO job : jobsOnPage) {
                allJobs.putIfAbsent(job.url(), job);
            }

            if (allJobs.size() == sizeBefore) {
                LOGGER.info("Job count did not increase (total remains {}). Page {} returned duplicate jobs. Stopping.", allJobs.size(), page);
                break;
            }

            pauseScraper();
        }
        return new java.util.ArrayList<>(allJobs.values());
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

    private void pauseScraper() {
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
