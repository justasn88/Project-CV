package lt.justasn88.jobcheckerapplication.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "scraper_executions")
public class ScraperExecutionEntity {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String scraperName;
private String status;

@Column(name = "jobs_found")
private Integer jobListingsFound;

private String errorMessage;

@Column(name = "executed_at", insertable = false, updatable = false)
private LocalDateTime executedAt;
}
