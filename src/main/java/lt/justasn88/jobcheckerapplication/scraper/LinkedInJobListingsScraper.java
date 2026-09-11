package lt.justasn88.jobcheckerapplication.scraper;

import com.microsoft.playwright.Page;
import lt.justasn88.jobcheckerapplication.config.ScraperProperties;
import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LinkedInJobListingsScraper extends AbstractPlaywrightScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedInJobListingsScraper.class);
    private static final String PROVIDER_NAME = "linkedin";

    private final String targetUrl;
    private final String scraperName;
    private final LinkedInHtmlParser parser;

    public LinkedInJobListingsScraper(ScraperProperties properties, LinkedInHtmlParser parser) {
        ScraperProperties.Provider provider = properties.providers().get(PROVIDER_NAME);
        this.targetUrl = provider.url();
        this.scraperName = provider.name();
        this.parser = parser;
    }

    @Override
    public String getScraperName() {
        return this.scraperName;
    }

    @Override
    protected List<JobListingsDTO> fetchJobsFromPage(Page browserPage, int pageNum) {
        int startOffset = (pageNum - 1) * 25;
        String currentUrl = targetUrl + (targetUrl.contains("?") ? "&" : "?") + "start=" + startOffset;

        LOGGER.info("Navigating to LinkedIn: " + currentUrl);
        browserPage.navigate(currentUrl);

        try {
            browserPage.waitForSelector("ul.jobs-search__results-list");
            String htmlContent = browserPage.content();
            Document doc = Jsoup.parse(htmlContent);

            List<JobListingsDTO> jobsList = parser.parseJobs(doc);

            LOGGER.info("In LinkedIn page: " + pageNum + " found jobs: " + jobsList.size());
            return jobsList;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to read LinkedIn page " + pageNum + ": " + e.getMessage());
            throw e;
        }
    }

}