package lt.justasn88.JobCheckerApplication.scraper;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CVbankasHtmlParserTest {

    private CVbankasHtmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new CVbankasHtmlParser();
    }

    @Test
    void parseJobs_ShouldExtractJobsCorrectly_WhenValidHtmlIsProvided() {
        String html = """
                <html>
                    <body>
                        <a class="list_a" href="https://www.cvbankas.lt/java-developer-1">
                            <h3 class="list_h3">Java Developer</h3>
                        </a>
                        <a class="list_a" href="https://www.cvbankas.lt/spring-boot-guru-2">
                            <h3 class="list_h3">Spring Boot Engineer</h3>
                        </a>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

            List<JobListingsDTO> exprectedJobs = List.of(
                    new JobListingsDTO("Java Developer", "https://www.cvbankas.lt/java-developer-1"),
                    new JobListingsDTO("Spring Boot Engineer", "https://www.cvbankas.lt/spring-boot-guru-2")
            );
            assertEquals(exprectedJobs, result, "The parsed job list should perfectly match the expected list\"");
    }

    @Test
    void parseJobs_ShouldReturnEmptyList_WhenNoJobElementsFound() {
        String emptyHtml = """
                <html>
                    <body>
                        <div>No job listings currently available</div>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(emptyHtml);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertNotNull(result, "The list should not be null");
        assertTrue(result.isEmpty(), "The list should be empty because no matching HTML elements were found");
    }

    @Test
    void parseJobs_ShouldHandleMissingTitlesGracefully() {
        String malformedHtml = """
                <html>
                    <body>
                        <a class="list_a" href="https://www.cvbankas.lt/broken-job">
                            <p>Job listing without a title tag</p>
                        </a>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(malformedHtml);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertEquals(1, result.size(), "Should find exactly 1 job listing despite the missing title");
        assertEquals("https://www.cvbankas.lt/broken-job", result.get(0).url());

        assertEquals("", result.get(0).title(), "If h3 is not found, the title should be an empty string");
    }
}