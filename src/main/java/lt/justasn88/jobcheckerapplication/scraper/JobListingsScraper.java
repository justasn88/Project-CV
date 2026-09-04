package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import java.util.List;

public interface JobListingsScraper {
    String getScraperName();
    List<JobListingsDTO> performScrape() throws Exception;
}