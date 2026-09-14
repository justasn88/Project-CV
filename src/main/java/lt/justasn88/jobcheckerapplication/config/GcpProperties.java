package lt.justasn88.jobcheckerapplication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gcp")
public record GcpProperties(
        String projectId,
        String region,
        String queueName,
        String serviceAccountEmail
) {
}