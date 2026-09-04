package lt.justasn88.jobcheckerapplication.repository;

import lt.justasn88.jobcheckerapplication.model.JobListingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JobListingsRepository extends JpaRepository<JobListingsEntity, Long> {
    @Query("SELECT j.url FROM JobListingsEntity j WHERE j.url IN :urls")
    Set<String> findExistingUrls(@Param("urls") List<String> urls);
}
