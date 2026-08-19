package lt.justasn88.JobCheckerApplication.repository;

import lt.justasn88.JobCheckerApplication.model.JobListingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobListingsRepository extends JpaRepository<JobListingsEntity, Long> {
    boolean existsByUrl(String url);
}
