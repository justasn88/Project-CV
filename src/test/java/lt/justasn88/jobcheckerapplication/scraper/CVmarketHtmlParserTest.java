package lt.justasn88.jobcheckerapplication.scraper;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CVmarketHtmlParserTest {

    private CVmarketHtmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new CVmarketHtmlParser();
    }

    @Test
    void parseJobs_ShouldExtractJobsAndHandleUrlsCorrectly() {
        String html = """
                <html>
                    <body>
                        <!-- Job ad with a relative URL -->
                        <a class="jobad-url" href="/job-java-developer">
                            <div class="font-extrabold">Java Developer</div>
                        </a>
                        <!-- Job ad with an absolute URL -->
                        <a class="jobad-url" href="https://www.cvmarket.lt/job-spring-boot">
                            <div class="font-extrabold">Spring Boot Specialist</div>
                        </a>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertEquals(2, result.size(), "Should extract exactly 2 job listings");

        assertEquals("Java Developer", result.get(0).title());
        assertEquals("https://www.cvmarket.lt/job-java-developer", result.get(0).url(), "Should prepend base URL to relative links");

        assertEquals("Spring Boot Specialist", result.get(1).title());
        assertEquals("https://www.cvmarket.lt/job-spring-boot", result.get(1).url(), "Should not modify absolute links");
    }

    @Test
    void parseJobs_ShouldFilterOutEmptyTitles() {
        String html = """
                <html>
                    <body>
                        <a class="jobad-url" href="/empty-job-ad">
                            <!-- Missing div.font-extrabold element -->
                        </a>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertTrue(result.isEmpty(), "Should return an empty list because the job title is missing");
    }

    @Test
    void parseJobs_ShouldExtractJobsFromRealHtmlFile() throws Exception {
        Path filePath = Paths.get("src/test/resources/cvmarket_sample.html");

        String realHtmlContent = Files.readString(filePath);

        Document mockDocument = Jsoup.parse(realHtmlContent);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertNotNull(result, "The result list should not be null");
        assertFalse(result.isEmpty(), "The parser should find at least one job in the real HTML file");

        assertEquals("Java Engineer in Digital Core (Ogre)", result.get(0).title(), "Should correctly extract the title from the real HTML block");
    }
}