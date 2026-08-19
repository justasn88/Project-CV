package lt.justasn88.JobCheckerApplication.scraper;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import lt.justasn88.JobCheckerApplication.service.JobListingsService;
import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractJobListingsScraperTimeTest {

    @Test
    void calculateNextExecutionTime_returnsTimeWithinBounds() {
        JobNotificationManager mockManager = Mockito.mock(JobNotificationManager.class);
        TaskScheduler mockScheduler = Mockito.mock(TaskScheduler.class);
        JobListingsService mockService = Mockito.mock(JobListingsService.class);


        ScraperProperties.Delay delay = new ScraperProperties.Delay(5000, 60000);

        ScraperProperties.Provider mockProvider = new ScraperProperties.Provider(
                "http://test.com",
                "0 0 * * * *",
                "TestScrapper",
                delay
        );

        AbstractJobListingsScraper myScraper = new AbstractJobListingsScraper(
                mockManager,
                mockScheduler,
                mockProvider,
                "Test-User-Agent",
                mockService
        ) {
            @Override
            public List<JobListingsDTO> extractJobListings(Document document) {
                return List.of();
            }
        };

        Instant beforeExecution = Instant.now();
        Instant nextExecutionTime = myScraper.calculateNextExecutionTime();

        long delayInMs = nextExecutionTime.toEpochMilli() - beforeExecution.toEpochMilli();

        assertTrue(delayInMs >= 5000, "Delay is too short! Was: " + delayInMs);
        assertTrue(delayInMs <= 60000 + 100, "Delay is too long! Was: " + delayInMs);
    }
}
