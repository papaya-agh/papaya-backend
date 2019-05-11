package pl.edu.agh.papaya.service.sprint

import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import pl.edu.agh.papaya.TestUtils
import pl.edu.agh.papaya.model.Project
import pl.edu.agh.papaya.model.Sprint
import pl.edu.agh.papaya.model.SprintState
import pl.edu.agh.papaya.repository.ProjectRepository
import pl.edu.agh.papaya.repository.SprintRepository
import spock.lang.Specification
import spock.lang.Unroll

@Configuration
@ComponentScan('pl.edu.agh.papaya')
@EnableAutoConfiguration
@EntityScan('pl.edu.agh.papaya.model')
@EnableJpaRepositories('pl.edu.agh.papaya.repository')
@SpringBootTest(classes = [SprintService])
class SprintServiceSpec extends Specification {

    private static final int ENROLLMENT_START = 100
    private static final int ENROLLMENT_END = 200
    private static final int DURATION_START = 300
    private static final int DURATION_END = 400
    private static final int CLOSED = 500

    @Autowired
    private SprintService sprintService

    @Autowired
    private SprintRepository sprintRepository

    @Autowired
    private ProjectRepository projectRepository

    @Unroll
    def "correctly fetches one sprint without a closing date from the repository"(int currentTime,
            SprintState requestedState) {
        setup: 'created a new project'
        Project project = new Project()
        and: 'saved the project to the project repository'
        projectRepository.save(project)
        and: 'created a new non-closed sprint'
        Sprint sprint = createSprint(false, project)
        and: 'added the sprint to the sprint repository'
        sprintRepository.save(sprint)

        when: 'the sprint service queries the sprint repository at a specified time to find all elements by their state'
        List<Sprint> sprints = sprintService.
                getByState(requestedState, TestUtils.createUtcLocalDateTimeFromEpochSeconds(currentTime))

        then: 'the sprint added to the repository is retrieved'
        sprints.size() == 1

        cleanup:
        sprintRepository.deleteAll()
        projectRepository.deleteAll()

        where:
        currentTime | requestedState
        0           | SprintState.UPCOMING
        50          | SprintState.UPCOMING
        100         | SprintState.DECLARABLE
        150         | SprintState.DECLARABLE
        200         | SprintState.PADDING
        250         | SprintState.PADDING
        300         | SprintState.IN_PROGRESS
        350         | SprintState.IN_PROGRESS
        400         | SprintState.FINISHED
        450         | SprintState.FINISHED
        500         | SprintState.FINISHED
        550         | SprintState.FINISHED
    }

    @Unroll
    def "correctly fetches one sprint with a closing date from the repository"(int currentTime,
            SprintState requestedState) {
        setup: 'created a new project'
        Project project = new Project()
        and: 'saved the project to the project repository'
        projectRepository.save(project)
        and: 'created a new closed sprint'
        Sprint sprint = createSprint(true, project)
        and: 'added the sprint to the sprint repository'
        sprintRepository.save(sprint)

        when: 'the sprint service queries the sprint repository at a specified time to find all elements by their state'
        List<Sprint> sprints = sprintService.
                getByState(requestedState, TestUtils.createUtcLocalDateTimeFromEpochSeconds(currentTime))

        then: 'the sprint added to the repository is retrieved'
        sprints.size() == 1

        cleanup:
        sprintRepository.deleteAll()
        projectRepository.deleteAll()

        where:
        currentTime | requestedState
        0           | SprintState.UPCOMING
        50          | SprintState.UPCOMING
        100         | SprintState.DECLARABLE
        150         | SprintState.DECLARABLE
        200         | SprintState.PADDING
        250         | SprintState.PADDING
        300         | SprintState.IN_PROGRESS
        350         | SprintState.IN_PROGRESS
        400         | SprintState.FINISHED
        450         | SprintState.FINISHED
        500         | SprintState.CLOSED
        550         | SprintState.CLOSED
    }

    @Unroll
    def "finds a correct number of sprints with each state (#expectedResults)"(List<List<Integer>> sprints,
            Map<String, Integer> expectedResults) {
        setup: 'current time was set'
        LocalDateTime currentTime = TestUtils.createUtcLocalDateTimeFromEpochSeconds(10)
        and: 'a new project was created'
        Project project = new Project()
        and: 'the project was saved to the project repository'
        projectRepository.save(project)
        and: 'sprints were created and saved to the sprint repository'
        sprints.each {
            Sprint sprint = createSprint(project, it[0], it[1], it[2], it[3], it[4])
            sprintRepository.save(sprint)
        }

        when: 'all sprint states are searched for'
        Map<SprintState, Integer> results = [:]
        SprintState.values().each {
            results.put(it, sprintService.getByState(it, currentTime).size())
        }

        then: 'the number of sprints with each state will match the expected number'
        results.every {
            (it.value == 0 && !expectedResults.containsKey(it.key)) || it.value == expectedResults.get(it.key)
        }

        cleanup:
        sprintRepository.deleteAll()
        projectRepository.deleteAll()

        where:
        sprints                                        | expectedResults
        [[null, 1, 2, 3, 4], [10, 1, 2, 3, 4]]         | [(SprintState.FINISHED): 1, (SprintState.CLOSED): 1]
        [[null, 11, 12, 13, 14], [0, 0, 0, 0, 0]]      | [(SprintState.UPCOMING): 1, (SprintState.CLOSED): 1]
        [[100, 11, 12, 13, 14], [null, 0, 0, 0, 0]]    | [(SprintState.UPCOMING): 1, (SprintState.FINISHED): 1]
        [[15, 1, 12, 13, 14], [0, 0, 0, 0, 0]]         | [(SprintState.DECLARABLE): 1, (SprintState.CLOSED): 1]
        [[null, 1, 12, 13, 14], [null, 0, 0, 0, 0]]    | [(SprintState.DECLARABLE): 1, (SprintState.FINISHED): 1]
        [[null, 9, 10, 13, 14], [0, 0, 0, 0, 0]]       | [(SprintState.PADDING): 1, (SprintState.CLOSED): 1]
        [[100, 9, 10, 13, 14], [null, 0, 0, 0, 0]]     | [(SprintState.PADDING): 1, (SprintState.FINISHED): 1]
        [[15, 8, 9, 10, 14], [0, 0, 0, 0, 0]]          | [(SprintState.IN_PROGRESS): 1, (SprintState.CLOSED): 1]
        [[null, 8, 9, 10, 14], [null, 0, 0, 0, 0]]     | [(SprintState.IN_PROGRESS): 1, (SprintState.FINISHED): 1]
        [[null, 9, 10, 13, 14], [14, 8, 9, 10, 13]]    | [(SprintState.PADDING): 1, (SprintState.IN_PROGRESS): 1]
        [[null, 9, 10, 13, 14], [14, 11, 12, 13, 13]]  | [(SprintState.PADDING): 1, (SprintState.UPCOMING): 1]
        [[null, 9, 10, 13, 14], [14, 9, 12, 13, 13]]   | [(SprintState.PADDING): 1, (SprintState.DECLARABLE): 1]
        [[null, 11, 12, 13, 14], [14, 10, 11, 12, 13]] | [(SprintState.UPCOMING): 1, (SprintState.DECLARABLE): 1]
        [[null, 11, 12, 13, 14], [14, 7, 8, 9, 13]]    | [(SprintState.UPCOMING): 1, (SprintState.IN_PROGRESS): 1]
        [[14, 9, 12, 13, 13], [15, 8, 9, 10, 14],]     | [(SprintState.DECLARABLE): 1, (SprintState.IN_PROGRESS): 1]
        [[null, 11, 12, 13, 14], [14, 11, 12, 13, 14]] | [(SprintState.UPCOMING): 2]
        [[100, 9, 12, 13, 14], [null, 10, 11, 12, 13]] | [(SprintState.DECLARABLE): 2]
        [[null, 9, 10, 13, 14], [14, 9, 10, 13, 14]]   | [(SprintState.PADDING): 2]
        [[100, 8, 9, 10, 14], [null, 8, 9, 10, 13]]    | [(SprintState.IN_PROGRESS): 2]
        [[5, 1, 2, 3, 4], [10, 1, 2, 3, 4]]            | [(SprintState.CLOSED): 2]
        [[null, 1, 2, 3, 4], [11, 1, 2, 3, 4]]         | [(SprintState.FINISHED): 2]
    }

    def createSprint(boolean closed, Project project = null) {
        createSprint(project, closed ? CLOSED : null)
    }

    @SuppressWarnings('ParameterCount')
    def createSprint(Project project, Integer dateClosed, int enrollmentStart = ENROLLMENT_START,
            int enrollmentEnd = ENROLLMENT_END, int durationStart = DURATION_START, int durationEnd = DURATION_END) {
        Sprint sprint = new Sprint()
        sprint.enrollmentPeriod = TestUtils.
                createUtcLocalDateTimePeriodFromEpochSeconds(enrollmentStart, enrollmentEnd)
        sprint.durationPeriod = TestUtils.
                createUtcLocalDateTimePeriodFromEpochSeconds(durationStart, durationEnd)
        sprint.dateClosed = (dateClosed != null) ? TestUtils.createUtcLocalDateTimeFromEpochSeconds(dateClosed) : null
        sprint.project = project
        sprint
    }
}
