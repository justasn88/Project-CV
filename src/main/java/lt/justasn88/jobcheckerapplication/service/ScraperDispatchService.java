package lt.justasn88.jobcheckerapplication.service;

import com.google.cloud.tasks.v2.*;
import com.google.protobuf.Timestamp;
import lt.justasn88.jobcheckerapplication.config.GcpProperties;
import lt.justasn88.jobcheckerapplication.config.ScraperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public class ScraperDispatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperDispatchService.class);
    private static final int DEFAULT_MIN_DELAY = 15;
    private static final int DEFAULT_MAX_DELAY = 45;

    private final ScraperProperties scraperProperties;
    private final GcpProperties gcpProperties;

    public ScraperDispatchService(ScraperProperties scraperProperties, GcpProperties gcpProperties) {
        this.scraperProperties = scraperProperties;
        this.gcpProperties = gcpProperties;
    }

    public int dispatchTask(String targetScraper, String host) {
        int delaySeconds = calculateDelay(targetScraper);
        String dynamicTargetUrl = "https://" + host + "/api/scrape";

        LOGGER.info("Cloud Scheduler triggered {}. Enqueuing task to {} with {}s delay.", targetScraper, dynamicTargetUrl, delaySeconds);

        try (CloudTasksClient client = CloudTasksClient.create()) {
            String queuePath = QueueName.of(gcpProperties.projectId(), gcpProperties.region(), gcpProperties.queueName()).toString();

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                    .setUrl(dynamicTargetUrl)
                    .setHttpMethod(HttpMethod.POST)
                    .putHeaders("TARGET_SCRAPER", targetScraper);

            if (gcpProperties.serviceAccountEmail() != null && !gcpProperties.serviceAccountEmail().isEmpty()) {
                httpRequestBuilder.setOidcToken(OidcToken.newBuilder().setServiceAccountEmail(gcpProperties.serviceAccountEmail()).build());
            }

            Instant scheduleTime = Instant.now().plusSeconds(delaySeconds);
            Task.Builder taskBuilder = Task.newBuilder()
                    .setHttpRequest(httpRequestBuilder)
                    .setScheduleTime(Timestamp.newBuilder()
                            .setSeconds(scheduleTime.getEpochSecond())
                            .setNanos(scheduleTime.getNano())
                            .build());

            client.createTask(queuePath, taskBuilder.build());
            return delaySeconds;
        } catch (Exception e) {
            LOGGER.error("Failed to enqueue task for {}", targetScraper, e);
            throw new RuntimeException("Could not dispatch task to Cloud Tasks", e);
        }
    }

    private int calculateDelay(String targetScraper) {
        int minDelay = DEFAULT_MIN_DELAY;
        int maxDelay = DEFAULT_MAX_DELAY;

        if (!"ALL".equalsIgnoreCase(targetScraper) && scraperProperties.providers().containsKey(targetScraper)) {
            ScraperProperties.Delay delay = scraperProperties.providers().get(targetScraper).delay();
            if (delay != null) {
                minDelay = delay.min();
                maxDelay = delay.max();
            }
        }
        return new Random().nextInt((maxDelay - minDelay) + 1) + minDelay;
    }
}