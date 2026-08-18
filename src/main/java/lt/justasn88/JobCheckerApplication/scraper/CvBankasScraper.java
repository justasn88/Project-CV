package lt.justasn88.JobCheckerApplication.scraper;

import jakarta.annotation.PostConstruct;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobDTO;

import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class CvBankasScraper extends AbstractJobScraper{

    private static final Logger LOGGER = LoggerFactory.getLogger(CvBankasScraper.class);

    private final ScraperProperties.Provider cvBankasConfig;
    private final CVbankasHtmlParser parser;

    public CvBankasScraper(ScraperProperties properties,
                           CVbankasHtmlParser parser,
                           JobNotificationManager notificationManager,
                           TaskScheduler taskScheduler) {

        super(
                notificationManager,
                taskScheduler,
                properties.getProviders().get("cvbankas"),
                properties.getUserAgent()
        );

        this.cvBankasConfig = properties.getProviders().get("cvbankas");
        this.parser = parser;
    }

    @PostConstruct
    public void initSchedule() {
        registerCronSchedule(cvBankasConfig.getCron());
    }

    @Override
    public List<JobDTO> extractJobListings(Document document) {
        return parser.parseJobs(document);
    }


}
