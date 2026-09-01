package lt.justasn88.JobCheckerApplication.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "jobs")
public class JobListingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(name = "scraper_name", nullable = false, length = 50)
    private String scraperName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobListingsEntity that)) return false;
        return url != null && url.equals(that.getUrl());
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : getClass().hashCode();
    }
}
