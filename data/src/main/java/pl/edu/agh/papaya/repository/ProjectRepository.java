package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import pl.edu.agh.papaya.model.Project;

public interface ProjectRepository extends CrudRepository<Project, Long> {

}
