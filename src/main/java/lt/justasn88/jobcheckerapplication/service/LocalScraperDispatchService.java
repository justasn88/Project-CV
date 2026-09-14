package lt.justasn88.jobcheckerapplication.service;

import lt.justasn88.jobcheckerapplication.config.ScraperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Profile("local")
public class LocalScraperDispatchService implements ScraperDispatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalScraperDispatchService.class);

    private final ScraperProperties scraperProperties;
    private final RestClient restClient;

    public LocalScraperDispatchService(ScraperProperties scraperProperties) {
        this.scraperProperties = scraperProperties;
        this.restClient = RestClient.create();
    }

    @Override
    public void dispatchTask(String targetScraper, String host) {
        if ("ALL".equalsIgnoreCase(targetScraper)) {
            LOGGER.info("[LOCAL] Dispatching individual tasks for each scraper...");
            for (String scraperName : scraperProperties.providers().keySet()) {
                simulateCloudTask(scraperName, host, 1, calculateDelay(scraperName));
            }
        } else {
            simulateCloudTask(targetScraper, host, 1, calculateDelay(targetScraper));
        }
    }

    @Override
    public void dispatchPaginationTask(String targetScraper, String host, int nextPage) {
        int delaySeconds = 4 + new Random().nextInt(7);
        simulateCloudTask(targetScraper, host, nextPage, delaySeconds);
    }

    private void simulateCloudTask(String targetScraper, String host, int targetPage, int delaySeconds) {
        String targetUrl = "http://" + host + "/api/scrape";
        LOGGER.info("[LOCAL] Scheduling task for {} (Page: {}) with {}s delay.", targetScraper, targetPage, delaySeconds);

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(delaySeconds);
                LOGGER.info("[LOCAL] Executing scheduled task -> {}", targetUrl);

                restClient.post()
                        .uri(targetUrl)
                        .header("TARGET_SCRAPER", targetScraper)
                        .header("TARGET_PAGE", String.valueOf(targetPage))
                        .header("Host", host)
                        .retrieve()
                        .toBodilessEntity();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("[LOCAL] Scheduled task was interrupted", e);
            } catch (Exception e) {
                LOGGER.error("[LOCAL] Failed to execute local task for {}", targetScraper, e.getMessage());
            }
        });
    }

    private int calculateDelay(String targetScraper) {
        int minDelay = 15;
        int maxDelay = 45;
        if (scraperProperties.providers().containsKey(targetScraper)) {
            ScraperProperties.Delay delay = scraperProperties.providers().get(targetScraper).delay();
            if (delay != null) {
                minDelay = delay.min();
                maxDelay = delay.max();
            }
        }
        return new Random().nextInt((maxDelay - minDelay) + 1) + minDelay;
    }
}