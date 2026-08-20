package lt.justasn88.JobCheckerApplication.scraper;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DelayTest {

    private final ScraperOrchestrator orchestrator = new ScraperOrchestrator(null, null, null, null, null);

    @Test
    void calculateRandomDelay_returnsTimeWithinBounds() {
        int min = 5000;
        int max = 60000;

        for(int i = 0; i < 100; i++) {
            long delay = orchestrator.calculateRandomDelay(min, max);

            assertTrue(delay >= min, "Delay is too short. Was :" +delay);
            assertTrue(delay <= max, "Delay is too long. Was :" +delay);
        }
    }
    @Test
    void calculateRandomDelay_whenMinEqualsMax_returnsMin() {
        int min = 10000;
        int max = 10000;

        long delay = orchestrator.calculateRandomDelay(min, max);

        assertEquals(10000, delay, "Should return min value when min equals max");
    }

    @Test
    void calculateRandomDelay_WhenMinGreatedThanMax_returnsMin() {
        int min = 15000;
        int max = 5000;

        long delay = orchestrator.calculateRandomDelay(min, max);

        assertEquals(15000, delay, "Should fallback to min value when config is invalid");
    }
}
