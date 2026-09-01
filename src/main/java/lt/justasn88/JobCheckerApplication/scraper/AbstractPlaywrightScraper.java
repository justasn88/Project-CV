package lt.justasn88.JobCheckerApplication.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPlaywrightScraper implements JobListingsScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlaywrightScraper.class);

    private static final int MAX_PAGES = 5;

    @Override
    public List<JobListingsDTO> performScrape() {
        Set<JobListingsDTO> allJobs = new LinkedHashSet<>();

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(List.of("--disable-dev-shm-usage"))
            );
            Page browserPage = browser.newPage();

            for (int page = 1; page <= MAX_PAGES; page++) {
                List<JobListingsDTO> jobsOnPage = fetchJobsFromPage(browserPage, page);

                if (jobsOnPage.isEmpty()) {
                    LOGGER.info("End of pagination (empty page) for " + getScraperName());
                    break;
                }

                int sizeBefore = allJobs.size();
                allJobs.addAll(jobsOnPage);

                if (allJobs.size() == sizeBefore) {
                    LOGGER.info("Job count did not increase. Page " + page + " returned duplicate jobs. Stopping.");
                    break;
                }

                pauseToAvoidBan();
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error when scraping with Playwright: {}", e.getMessage(), e);
            throw e;
        }

        return new ArrayList<>(allJobs);
    }

    protected abstract List<JobListingsDTO> fetchJobsFromPage(Page browserPage, int pageNum);

    protected void pauseToAvoidBan() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}