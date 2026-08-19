package lt.justasn88.JobCheckerApplication.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraper_executions")
@Data
public class ScraperExecutionEntity {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String scraperName;
private String status;
private Integer JobListingsFound;
private String errorMessage;

@Column(name = "executed_at", insertable = false, updatable = false)
private LocalDateTime executedAt;
}
