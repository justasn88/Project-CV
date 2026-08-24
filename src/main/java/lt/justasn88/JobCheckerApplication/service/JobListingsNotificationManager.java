package lt.justasn88.JobCheckerApplication.service;

import lt.justasn88.JobCheckerApplication.model.JobListingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    public void notifyUser(JobListingsDTO job) {
        LOGGER.info("New Job Found: {}\nLink: {}\n---------------------------------------------------",
                job.title(),
                job.url());

        String message = String.format("<b>New job listing!</b>\n\n %s\n %s", job.title(), job.url());
       sendMessageToTelegram(message);
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
        }catch (Exception e) {
            LOGGER.error("Failed to send Telegram message");
        }
    }
}