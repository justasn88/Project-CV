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
import java.util.Map;

public abstract class AbstractPlaywrightScraper implements JobListingsScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlaywrightScraper.class);

    private final String userAgent;
    private final Map<String, String> headers;

    AbstractPlaywrightScraper(String userAgent, Map<String, String> headers) {
        this.userAgent = userAgent;
        this.headers = headers;
    }

    @Override
    public List<JobListingsDTO> performScrape(int page) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(true)
                             .setArgs(List.of(
                                     "--disable-dev-shm-usage",
                                     "--no-sandbox",
                                     "--disable-gpu"
                             ))
             )) {

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent(this.userAgent)
                    .setViewportSize(1920, 1080);

            if (this.headers != null && !this.headers.isEmpty()) {
                contextOptions.setExtraHTTPHeaders(this.headers);
            }

            try (BrowserContext context = browser.newContext(contextOptions)) {
                context.setDefaultTimeout(15000);
                Page browserPage = context.newPage();
                browserPage.route("**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2,ttf,eot}", route -> route.abort());

                LOGGER.info("Searching for jobs: {} page: {}", getScraperName(), page);
                List<JobListingsDTO> jobsOnPage = fetchJobsFromPage(browserPage, page);

                if (jobsOnPage.isEmpty()) {
                    LOGGER.info("End of pagination (empty page) for " + getScraperName());
                }
                return jobsOnPage;
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error when scraping with Playwright: {}", e.getMessage());
            throw e;
        }
    }

    protected abstract List<JobListingsDTO> fetchJobsFromPage(Page browserPage, int pageNum);

}