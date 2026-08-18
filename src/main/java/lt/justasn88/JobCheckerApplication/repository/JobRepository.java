package lt.justasn88.JobCheckerApplication.repository;

import lt.justasn88.JobCheckerApplication.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
    boolean existsByUrl(String url);
}
