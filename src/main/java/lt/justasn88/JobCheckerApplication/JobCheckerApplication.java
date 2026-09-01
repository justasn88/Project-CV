package lt.justasn88.JobCheckerApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("lt.justasn88.JobCheckerApplication.config")
public class JobCheckerApplication {
	public static void main(String[] args) {
		SpringApplication.run(JobCheckerApplication.class, args);
	}
}