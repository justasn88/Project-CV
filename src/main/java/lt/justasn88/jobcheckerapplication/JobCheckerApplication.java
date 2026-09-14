package lt.justasn88.jobcheckerapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("lt.justasn88.jobcheckerapplication.config")
public class JobCheckerApplication {
	public static void main(String[] args) {
		SpringApplication.run(JobCheckerApplication.class, args);
	}
}