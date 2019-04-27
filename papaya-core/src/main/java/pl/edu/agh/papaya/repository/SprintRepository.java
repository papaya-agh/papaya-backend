package pl.edu.agh.papaya.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.util.OptionalUtil;

@Repository
public interface SprintRepository extends CrudRepository<Sprint, Long> {

    @Query("select s from Sprint s where s.enrollmentPeriod.start > ?1")
    List<Sprint> findUpcoming(LocalDateTime localDateTime);

    @Query("select s from Sprint s where s.enrollmentPeriod.start <= ?1 and s.enrollmentPeriod.end > ?1")
    List<Sprint> findDeclarable(LocalDateTime localDateTime);

    @Query("select s from Sprint s where s.enrollmentPeriod.end <= ?1 and s.durationPeriod.start > ?1")
    List<Sprint> findPadding(LocalDateTime localDateTime);

    @Query("select s from Sprint s where s.durationPeriod.start <= ?1 and s.durationPeriod.end > ?1")
    List<Sprint> findInProgress(LocalDateTime localDateTime);

    @Query("select s from Sprint s where s.durationPeriod.end <= ?1 and (s.dateClosed is null or s.dateClosed > ?1)")
    List<Sprint> findFinished(LocalDateTime localDateTime);

    @Query("select s from Sprint s where s.dateClosed is not null and s.dateClosed <= ?1")
    List<Sprint> findClosed(LocalDateTime localDateTime);

    @Query("select s from Sprint s where s.dateClosed is null or s.dateClosed > ?1")
    List<Sprint> findNotClosed(LocalDateTime localDateTime);

    default Optional<Sprint> findLastInProject(Long projectId) {
        return OptionalUtil.fromCollection(findInProjectOrderedFromNewest(projectId, PageRequest.of(0, 1)));
    }

    @Query("select s from Sprint s where s.project.id = ?1 order by s.durationPeriod.start desc")
    List<Sprint> findInProjectOrderedFromNewest(Long projectId, Pageable limit);
}
