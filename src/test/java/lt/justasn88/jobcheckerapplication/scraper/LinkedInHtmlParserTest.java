package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkedInHtmlParserTest {

    private LinkedInHtmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new LinkedInHtmlParser();
    }

    @Test
    void parseJobs_ShouldExtractJobsAndCleanUrlsCorrectly() {
        String html = """
                <html>
                    <body>
                        <ul class="jobs-search__results-list">
                            <li>
                                <div class="base-search-card__info">
                                    <h3 class="base-search-card__title">Java Software Engineer</h3>
                                </div>
                                <a class="base-card__full-link" href="https://www.linkedin.com/jobs/view/123456?trk=public_jobs"></a>
                            </li>
                            <li>
                                <div class="base-search-card__info">
                                    <h3 class="base-search-card__title">Backend Developer (Spring Boot)</h3>
                                </div>
                                <a class="base-card__full-link" href="https://www.linkedin.com/jobs/view/789012"></a>
                            </li>
                        </ul>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertEquals(2, result.size(), "Should extract exactly 2 job listings");

        assertEquals("Java Software Engineer", result.get(0).title());
        assertEquals("https://www.linkedin.com/jobs/view/123456", result.get(0).url(), "Should remove query parameters from the URL");

        assertEquals("Backend Developer (Spring Boot)", result.get(1).title());
        assertEquals("https://www.linkedin.com/jobs/view/789012", result.get(1).url());
    }

    @Test
    void parseJobs_ShouldFilterOutEmptyOrInvalidListings() {
        String html = """
                <html>
                    <body>
                        <ul class="jobs-search__results-list">
                            <li>
                                <!-- Missing title -->
                                <a class="base-card__full-link" href="https://www.linkedin.com/jobs/view/111"></a>
                            </li>
                            <li>
                                <!-- Missing link -->
                                <div class="base-search-card__info">
                                    <h3 class="base-search-card__title">Ghost Job</h3>
                                </div>
                            </li>
                        </ul>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertTrue(result.isEmpty(), "Should return an empty list because jobs lack either a title or a URL");
    }
}