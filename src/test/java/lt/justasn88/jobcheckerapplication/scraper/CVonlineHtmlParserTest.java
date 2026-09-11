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

class CVonlineHtmlParserTest {

    private CVonlineHtmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new CVonlineHtmlParser();
    }

    @Test
    void parseJobs_ShouldExtractJobsAndHandleUrlsCorrectly() {
        String html = """
                <html>
                    <body>
                        <h2 class="vacancy-item__title">
                            <a href="/vacancy/1652846/emplonet-uab/java-programuotojas-a" target="_self" data-testid="vacancy-item-link-title-1652846" class="jsx-689969871">Java programuotojas (-a)</a>
                        </h2>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertEquals(1, result.size(), "Should extract exactly 1 job listing");

        assertEquals("Java programuotojas (-a)", result.get(0).title());

        assertEquals("https://cvonline.lt/vacancy/1652846/emplonet-uab/java-programuotojas-a", result.get(0).url(), "Should prepend base URL to relative links");
    }

    @Test
    void parseJobs_ShouldFilterOutEmptyTitles() {
        String html = """
                <html>
                    <body>
                        <h2 class="vacancy-item__title">
                            <a href="/vacancy/empty-job" target="_self" class="jsx-689969871"></a>
                        </h2>
                    </body>
                </html>
                """;

        Document mockDocument = Jsoup.parse(html);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertTrue(result.isEmpty(), "Should return an empty list because the job title is missing");
    }

    @Test
    void parseJobs_ShouldExtractJobsFromRealHtmlFile() throws Exception {
        Path filePath = Paths.get("src/test/resources/cvonline_sample.html");

        if (!Files.exists(filePath)) {
            System.out.println("cvonline_sample.html not found, skipping this test.");
            return;
        }

        String realHtmlContent = Files.readString(filePath);

        Document mockDocument = Jsoup.parse(realHtmlContent);

        List<JobListingsDTO> result = parser.parseJobs(mockDocument);

        assertNotNull(result, "The result list should not be null");
        assertFalse(result.isEmpty(), "The parser should find at least one job in the real HTML file");
    }
}