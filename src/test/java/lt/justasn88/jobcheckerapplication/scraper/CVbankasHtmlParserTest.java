package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Imports required for reading the real HTML file
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        List<JobListingsDTO> expectedJobs = List.of(
                new JobListingsDTO("Java Developer", "https://www.cvbankas.lt/java-developer-1"),
                new JobListingsDTO("Spring Boot Engineer", "https://www.cvbankas.lt/spring-boot-guru-2")
        );
        assertEquals(expectedJobs, result, "The parsed job list should perfectly match the expected list");
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

    @Test
    void parseJobs_ShouldExtractJobsFromRealHtmlFile() throws Exception {
        Path filePath = Paths.get("src/test/resources/cvbankas_sample.html");

        if (!Files.exists(filePath)) {
            System.out.println("cvbankas_sample.html not found, skipping this test.");
            return;
        }

        String realHtmlContent = Files.readString(filePath);
        Document mockDocument = Jsoup.parse(realHtmlContent);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertNotNull(result, "The result list should not be null");
        assertEquals(2, result.size(), "The parser should find exactly 2 jobs in the sample file");

        assertEquals("Senior Java Developer (m/f/d) - Payment Project", result.get(0).title());
        assertEquals("https://www.cvbankas.lt/senior-java-developer-m-f-d-payment-project-vilniuje/1-13978260", result.get(0).url());

        assertEquals("JAVA PROGRAMUOTOJAS (-A)", result.get(1).title());
        assertEquals("https://www.cvbankas.lt/java-programuotojas-a-vilniuje/1-14091141", result.get(1).url());
    }
}