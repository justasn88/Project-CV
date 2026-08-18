package lt.justasn88.JobCheckerApplication.scraper;
import lt.justasn88.JobCheckerApplication.config.ScraperProperties;
import lt.justasn88.JobCheckerApplication.model.JobDTO;
import lt.justasn88.JobCheckerApplication.service.JobNotificationManager;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractJobScraperTimeTest {

    @Test
    void calculateNextExecutionTime_returnsTimeWithinBounds() {
        JobNotificationManager mockManager = Mockito.mock(JobNotificationManager.class);
        TaskScheduler mockScheduler = Mockito.mock(TaskScheduler.class);

        ScraperProperties properties = new ScraperProperties();
        ScraperProperties.Delay delay = new ScraperProperties.Delay();
        delay.setMin(5000);
        delay.setMax(60000);
        properties.setDelay(delay);

        AbstractJobScraper myScraper = new AbstractJobScraper(
                mockManager,
                mockScheduler,
                properties,
                "http://test.com",
                "TestScraper"
        ) {
            @Override
            public List<JobDTO> extractJobListings(Document document) {
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
