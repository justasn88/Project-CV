package lt.justasn88.jobcheckerapplication.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractPlaywrightScraper implements JobListingsScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlaywrightScraper.class);

    private static final int MAX_PAGES = 5;

    @Override
    public List<JobListingsDTO> performScrape(int page) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(true)
                             .setArgs(List.of("--disable-dev-shm-usage", "--no-sandbox"))
             );
             BrowserContext context = browser.newContext()) {

            Page browserPage = context.newPage();

            browserPage.route("**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2,ttf,eot}", route -> route.abort());

            LOGGER.info("Searching for jobs: {} page: {}", getScraperName(), page);

            List<JobListingsDTO> jobsOnPage = fetchJobsFromPage(browserPage, page);

            if (jobsOnPage.isEmpty()) {
                LOGGER.info("End of pagination (empty page) for " + getScraperName());
            }

            return jobsOnPage;

        } catch (RuntimeException e) {
            LOGGER.error("Error when scraping with Playwright: {}", e.getMessage(), e);
            throw e;
        }
    }

    protected abstract List<JobListingsDTO> fetchJobsFromPage(Page browserPage, int pageNum);

    protected void pauseScraper() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}