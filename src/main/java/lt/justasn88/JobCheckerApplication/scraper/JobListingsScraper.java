package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import java.util.List;

public interface JobListingsScraper {
    String getScraperName();
    List<JobListingsDTO> performScrape() throws Exception;
}