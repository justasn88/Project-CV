package lt.justasn88.jobcheckerapplication.repository;

import lt.justasn88.jobcheckerapplication.model.ScraperExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScraperExecutionRepository extends JpaRepository<ScraperExecutionEntity, Long> {
}
