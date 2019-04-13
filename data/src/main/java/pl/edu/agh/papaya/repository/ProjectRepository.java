package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Project;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

}
