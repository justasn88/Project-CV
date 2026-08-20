package lt.justasn88.JobCheckerApplication.scraper;

import lombok.Getter;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

public abstract class AbstractJobListingsScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobListingsScraper.class);

    @Getter
    private final String targetUrl;

    @Getter
    private final String scraperName;

    private final String userAgent;


    protected AbstractJobListingsScraper(String targetUrl,
                                         String scraperName,
                                         String userAgent){
        this.targetUrl = targetUrl;
        this.scraperName = scraperName;
        this.userAgent = userAgent;
    }


    public List<JobListingsDTO> performScrape() throws IOException {

        Document doc = Jsoup.connect(getTargetUrl())
                .userAgent(this.userAgent)
                .timeout(10000)
                .get();

        return extractJobListings(doc);
    }

    public abstract List<JobListingsDTO> extractJobListings (Document document);

}
