package lt.justasn88.JobCheckerApplication.scraper;

import jakarta.annotation.PostConstruct;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;

import lt.justasn88.JobCheckerApplication.service.JobListingsService;
import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class CvBankasJobListingsScraper extends AbstractJobListingsScraper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CvBankasJobListingsScraper.class);

    private final ScraperProperties.Provider cvBankasConfig;
    private final CVbankasHtmlParser parser;

    public CvBankasJobListingsScraper(ScraperProperties properties,
                                      CVbankasHtmlParser parser,
                                      JobNotificationManager notificationManager,
                                      TaskScheduler taskScheduler,
                                      JobListingsService jobListingsService) {

        super(
                notificationManager,
                taskScheduler,
                properties.providers().get("cvbankas"),
                properties.userAgent(),
                jobListingsService
                );

        this.cvBankasConfig = properties.providers().get("cvbankas");
        this.parser = parser;
    }

    @PostConstruct
    public void initSchedule() {
        registerCronSchedule(cvBankasConfig.cron());
    }

    @Override
    public List<JobListingsDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }


}
