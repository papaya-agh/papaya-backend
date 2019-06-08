package pl.edu.agh.papaya.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Sprint;

@Repository
public interface SprintRepository extends CrudRepository<Sprint, Long> {

    @Query("select s from Sprint s where s.enrollmentPeriod.start > ?1 and s.project.id = ?2")
    List<Sprint> findUpcomingInProject(LocalDateTime evaluationTime, Long projectId, Pageable pageable);

    @Query("select s from Sprint s where s.enrollmentPeriod.start <= ?1 and s.enrollmentPeriod.end > ?1 "
            + "and s.project.id = ?2")
    List<Sprint> findDeclarableInProject(LocalDateTime evaluationTime, Long projectId, Pageable pageable);

    @Query("select s from Sprint s where s.enrollmentPeriod.end <= ?1 and s.durationPeriod.start > ?1 "
            + "and s.project.id = ?2")
    List<Sprint> findPaddingInProject(LocalDateTime evaluationTime, Long projectId, Pageable pageable);

    @Query("select s from Sprint s where s.durationPeriod.start <= ?1 and s.durationPeriod.end > ?1 "
            + "and s.project.id = ?2")
    List<Sprint> findInProgressInProject(LocalDateTime evaluationTime, Long projectId, Pageable pageable);

    @Query("select s from Sprint s where s.durationPeriod.end <= ?1 and (s.dateClosed is null or s.dateClosed > ?1) "
            + "and s.project.id = ?2")
    List<Sprint> findFinishedInProject(LocalDateTime evaluationTime, Long projectId, Pageable pageable);

    @Query("select s from Sprint s where s.dateClosed is not null and s.dateClosed <= ?1 and s.project.id = ?2")
    List<Sprint> findClosedInProject(LocalDateTime evaluationTime, Long projectId, Pageable pageable);

    @Query("select s from Sprint s where s.enrollmentPeriod.start <= ?1 and s.enrollmentPeriod.end > ?1")
    List<Sprint> findAllDeclarable(LocalDateTime evaluationTime);

    @Query("select s from Sprint s where s.enrollmentPeriod.end <= ?1 and s.durationPeriod.start > ?1")
    List<Sprint> findAllPadding(LocalDateTime evaluationTime);

    Optional<Sprint> findFirstByProjectIdOrderByDurationPeriodStartDesc(Long projectId);

    default Optional<Sprint> findPrecedingSprint(Sprint sprint) {
        return findPrecedingSprints(sprint, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query("select s from Sprint s where s.project.id = :#{#sprint.project.id} "
            + "and s.durationPeriod.start < :#{#sprint.durationPeriod.start} order by s.durationPeriod.start desc")
    List<Sprint> findPrecedingSprints(@Param("sprint") Sprint sprint, Pageable pageable);

    default Optional<Sprint> findFollowingSprint(Sprint sprint) {
        return findFollowingSprints(sprint, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query("select s from Sprint s where s.project.id = :#{#sprint.project.id} "
            + "and s.durationPeriod.end > :#{#sprint.durationPeriod.end} order by s.durationPeriod.start asc")
    List<Sprint> findFollowingSprints(@Param("sprint") Sprint sprint, Pageable pageable);

    @Query("select avg(s.stats.coefficient) from Sprint s where s.project.id = ?1 and s.durationPeriod.start <= ?2")
    Optional<Double> findAverageSprintCoefficientInProjectUpToDate(Long projectId, LocalDateTime evaluationTime);
}
