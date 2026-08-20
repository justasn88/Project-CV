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
        final int MAX_PAGES = 10;

        for (int page = 1; page <= MAX_PAGES; page++) {
            String currentUrl = buildPageUrl(page);
            LOGGER.info("Searching for job listings: {} page: {}", scraperName, page);

            org.jsoup.Connection.Response response = Jsoup.connect(currentUrl)
                    .userAgent(this.userAgent)
                    .timeout(10000)
                    .execute();

            if (!response.url().toString().equals(currentUrl)) {
                LOGGER.info("Page redirected from {} to {}. Pagination finished.", currentUrl, response.url());
                break;
            }

            Document doc = response.parse();
            List<JobListingsDTO> jobsOnPage = extractJobListings(doc);

            if (jobsOnPage.isEmpty()) {
                LOGGER.info("Page empty. Search stopped. Job listings found: {}", allJobs.size());
                break;
            }

            allJobs.addAll(jobsOnPage);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Scraping forcefully terminated");
                break;
            }
        }

        return allJobs;
    }

    protected String buildPageUrl(int page) {
        if (page == 1) {
            return targetUrl;
        }
        return targetUrl + (targetUrl.contains("?") ? "&" : "?") + "page=" + page;
    }

    public abstract List<JobListingsDTO> extractJobListings (Document document);

}
