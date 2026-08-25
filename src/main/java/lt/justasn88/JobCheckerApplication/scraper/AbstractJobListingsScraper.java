package lt.justasn88.JobCheckerApplication.scraper;

import lombok.Getter;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJobListingsScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperOrchestrator.class);

    @Getter
    private final String targetUrl;

    @Getter
    private final String scraperName;

    private final String userAgent;


    protected AbstractJobListingsScraper(ScraperProperties.Provider providerConfig, String userAgent){
        this.targetUrl = providerConfig.url();
        this.scraperName = providerConfig.name();
        this.userAgent = userAgent;
    }


    public List<JobListingsDTO> performScrape() throws IOException {
        List<JobListingsDTO> allJobs = new ArrayList<>();
        final int MAX_PAGES = 5;

        for (int page = 1; page <= MAX_PAGES; page++) {
            List<JobListingsDTO> jobsOnPage = fetchJobsFromPage(page);
            if (jobsOnPage.isEmpty()) {
                LOGGER.info("End of pagination. Total jobs found: {}", allJobs.size());
                break;
            }

            allJobs.addAll(jobsOnPage);
            pauseToAvoidBan();
        }

        return allJobs;
    }

    private List<JobListingsDTO> fetchJobsFromPage(int page) throws IOException {
        String currentUrl = buildPageUrl(page);
        LOGGER.info("Searching for jobs: {} page: {}", scraperName, page);

        org.jsoup.Connection.Response response = Jsoup.connect(currentUrl)
                .userAgent(this.userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "lt,en-US;q=0.7,en;q=0.3")
                .timeout(30000)
                .ignoreHttpErrors(true)
                .execute();

        if (response.statusCode() == 404) {
            LOGGER.info("Page not found (404) for {}. Assuming end of pagination.", currentUrl);
            return List.of();
        }

        if (!response.url().toString().equals(currentUrl)) {
            LOGGER.info("Redirect detected from {} to {}. End of pagination.", currentUrl, response.url());
            return List.of();
        }

        return extractJobListings(response.parse());
    }

    private void pauseToAvoidBan() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Scraping forcefully terminated.");
        }
    }

    protected String buildPageUrl(int page) {
        if (page == 1) {
            return targetUrl;
        }
        return targetUrl + (targetUrl.contains("?") ? "&" : "?") + "page=" + page;
    }

    public abstract List<JobListingsDTO> extractJobListings (Document document);

}
