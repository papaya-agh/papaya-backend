package pl.edu.agh.papaya.model

import pl.edu.agh.papaya.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class SprintSpec extends Specification {

    private static final int ENROLLMENT_START = 100
    private static final int ENROLLMENT_END = 200
    private static final int DURATION_START = 300
    private static final int DURATION_END = 400
    private static final int CLOSED = 500

    @Unroll
    def "correctly evaluates the state of a sprint without a dateClosed set (#expectedState)"(int currentTime,
            SprintState expectedState) {
        given: 'a non-closed sprint was created'
        Sprint sprint = createSprint(false)

        when: 'the sprint state is evaluated at a specified time'
        SprintState sprintState = sprint.getSprintState(TestUtils.createUtcLocalDateTimeFromEpochSeconds(currentTime))

        then: 'the obtained state matches the expected state'
        sprintState == expectedState

        where:
        currentTime | expectedState
        0           | SprintState.UPCOMING
        100         | SprintState.DECLARABLE
        150         | SprintState.DECLARABLE
        200         | SprintState.PADDING
        250         | SprintState.PADDING
        300         | SprintState.IN_PROGRESS
        350         | SprintState.IN_PROGRESS
        400         | SprintState.FINISHED
        500         | SprintState.FINISHED
        550         | SprintState.FINISHED
    }

    @Unroll
    def "correctly evaluates the state (#expectedState) of a sprint whose dateClosed has been set"(int currentTime,
            SprintState expectedState) {
        given: 'a closed sprint was created'
        Sprint sprint = createSprint(true)

        when: 'the sprint state is evaluated at a specified time'
        SprintState sprintState = sprint.getSprintState(TestUtils.createUtcLocalDateTimeFromEpochSeconds(currentTime))

        then: 'the obtained state matches the expected state'
        sprintState == expectedState

        where:
        currentTime | expectedState
        0           | SprintState.UPCOMING
        100         | SprintState.DECLARABLE
        150         | SprintState.DECLARABLE
        200         | SprintState.PADDING
        250         | SprintState.PADDING
        300         | SprintState.IN_PROGRESS
        350         | SprintState.IN_PROGRESS
        400         | SprintState.FINISHED
        500         | SprintState.CLOSED
        550         | SprintState.CLOSED
    }

    def createSprint(boolean closed) {
        Sprint sprint = new Sprint()
        sprint.enrollmentPeriod = TestUtils.
                createUtcLocalDateTimePeriodFromEpochSeconds(ENROLLMENT_START, ENROLLMENT_END)
        sprint.durationPeriod = TestUtils.
                createUtcLocalDateTimePeriodFromEpochSeconds(DURATION_START, DURATION_END)
        sprint.dateClosed = closed ? TestUtils.createUtcLocalDateTimeFromEpochSeconds(CLOSED) : null
        sprint
    }
}
