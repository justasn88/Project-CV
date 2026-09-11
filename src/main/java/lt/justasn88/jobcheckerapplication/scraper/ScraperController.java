package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.service.ScraperDispatchService;
import lt.justasn88.jobcheckerapplication.service.ScraperExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class ScraperController {

    private final ScraperDispatchService dispatchService;
    private final ScraperExecutionService executionService;

    public ScraperController(ScraperDispatchService dispatchService, ScraperExecutionService executionService) {
        this.dispatchService = dispatchService;
        this.executionService = executionService;
    }

    @PostMapping("/dispatch")
    public ResponseEntity<String> dispatch(@RequestHeader("TARGET_SCRAPER") String targetScraper,
                                           HttpServletRequest request) {
        try {
            String host = request.getHeader("Host");
            dispatchService.dispatchTask(targetScraper, host);
            return ResponseEntity.ok("Dispatched tasks successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to dispatch task");
        }
    }

    @PostMapping("/scrape")
    public ResponseEntity<String> scrape(
            @RequestHeader("TARGET_SCRAPER") String targetScraper,
            @RequestHeader(value = "TARGET_PAGE", defaultValue = "1") int targetPage,
            HttpServletRequest request) {

        String host = request.getHeader("Host");
        executionService.executeTargetScrapers(targetScraper, targetPage, host);
        return ResponseEntity.ok("Scraping completed");
    }
}