package lt.justasn88.jobcheckerapplication.scraper;

import com.microsoft.playwright.Page;
import lt.justasn88.jobcheckerapplication.config.ScraperProperties;
import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class LinkedInScraper extends AbstractPlaywrightScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedInScraper.class);

    private static final String PROVIDER_NAME = "linkedin";
    private final String targetUrl;
    private final String scraperName;

    public LinkedInScraper(ScraperProperties properties) {
        ScraperProperties.Provider provider = properties.providers().get(PROVIDER_NAME);
        this.targetUrl = provider.url();
        this.scraperName = provider.name();
    }

    @Override
    public String getScraperName() {
        return this.scraperName;
    }

    @Override
    protected List<JobListingsDTO> fetchJobsFromPage(Page browserPage, int pageNum) {
        List<JobListingsDTO> jobsList = new ArrayList<>();

        int startOffset = (pageNum - 1) * 25;

        String currentUrl = targetUrl + (targetUrl.contains("?") ? "&" : "?") + "start=" + startOffset;

        LOGGER.info("Navigating to LinkedIn: " + currentUrl);
        browserPage.navigate(currentUrl);

        try {
            browserPage.waitForSelector("ul.jobs-search__results-list");

            String htmlContent = browserPage.content();
            Document doc = Jsoup.parse(htmlContent);

            Elements jobElements = doc.select("ul.jobs-search__results-list > li");

            for (Element el : jobElements) {
                String title = el.select("h3.base-search-card__title").text().trim();
                String jobUrl = el.select("a.base-card__full-link").attr("href");

                if (jobUrl.contains("?")) {
                    jobUrl = jobUrl.substring(0, jobUrl.indexOf("?"));
                }

                if (!title.isEmpty() && !jobUrl.isEmpty()) {
                    jobsList.add(new JobListingsDTO(title, jobUrl));
                }
            }

            LOGGER.info("In LinkedIn page: " + pageNum + " found jobs: " + jobsList.size());

        } catch (RuntimeException e) {
            LOGGER.error("Failed to read LinkedIn page " + pageNum + ": " + e.getMessage());
            throw e;
        }

        return jobsList;
    }

}