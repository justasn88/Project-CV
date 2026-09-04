package lt.justasn88.jobcheckerapplication.service;

import lt.justasn88.jobcheckerapplication.model.JobListingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class JobListingsNotificationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobListingsNotificationManager.class);

    private final RestClient restClient;
    private final String botToken;
    private final String chatId;

    public JobListingsNotificationManager (
            @Value("${telegram.bot-token}") String botToken,
            @Value("${telegram.chat-id}") String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.restClient = RestClient.create();
    }

    public void notifyUsers(List<JobListingsDTO> newJobs) {
        if (newJobs.isEmpty()) return;

        LOGGER.info("Found {} new job listings. Generating Telegram messages...", newJobs.size());

        final int MAX_MESSAGE_LENGTH = 4000;

        StringBuilder message = new StringBuilder("<b>New job listings found:</b>\n\n");

        for (JobListingsDTO job : newJobs) {
            LOGGER.info("New Job Found: {} | Link: {}", job.title(), job.url());

            String jobLine = String.format("🔹 <a href='%s'>%s</a>\n", job.url(), job.title());

            if (message.length() + jobLine.length() > MAX_MESSAGE_LENGTH) {
                sendMessageToTelegram(message.toString());
                message = new StringBuilder("<b>New job listings (continued):</b>\n\n");
            }

            message.append(jobLine);
        }
        sendMessageToTelegram(message.toString());
    }

    public void notifyFailure(String scraperName, String errorMessage) {
        LOGGER.warn("WARNING: Scraper '{}' failed, error: {}", scraperName, errorMessage);

        String message = String.format("<b>scraper error: %s</b>\n\n reason: %s", scraperName, errorMessage);
        sendMessageToTelegram(message);
    }

    private void sendMessageToTelegram(String text) {
        try {
            String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            restClient.post()
                    .uri(telegramApiUrl)
                    .body(Map.of(
                            "chat_id", chatId,
                            "text", text,
                            "parse_mode", "HTML"
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            LOGGER.error("Failed to send Telegram message", e);
            throw new RuntimeException("Telegram notification failed", e);
        }
    }
}