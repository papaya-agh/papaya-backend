package pl.edu.agh.papaya.rest.jira.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.JiraBoardDto;
import pl.edu.agh.papaya.api.model.JiraSprintDto;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintStats;
import pl.edu.agh.papaya.service.jira.JiraAuthorizationService;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.util.BadRequestException;
import pl.edu.agh.papaya.util.NotAcceptableException;

@Service
@RequiredArgsConstructor
public class JiraRestService {

    private static final int MAX_SPRINT_OFFSET = 60 * 24 * 30;
    private static final String JIRA_HOURS_ENDPOINT =
            "/rest/greenhopper/1.0/rapid/charts/sprintreport?sprintId=%d&rapidViewId=%d";
    private static final String JIRA_BOARDS_ENDPOINT = "/rest/agile/1.0/board";
    private static final String JIRA_SPRINTS_ENDPOINT = "/rest/agile/1.0/board/%d/sprint";
    private static final String STATE = "state";
    private static final String START_DATE = "startDate";
    private static final String CONTENTS = "contents";
    private static final String VALUE = "value";
    private static final String VALUES = "values";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String PROJECT_ID = "projectId";
    private static final String PROJECT_KEY = "projectKey";
    private static final String PROJECT_NAME = "projectName";
    private static final String LOCATION = "location";
    private static final String ISSUES_NOT_COMPLETED_INITIAL_ESTIMATE_SUM = "issuesNotCompletedInitialEstimateSum";
    private static final String ALL_ISSUES_ESTIMATE_SUM = "allIssuesEstimateSum";
    private static final String ISSUES_NOT_COMPLETED_ESTIMATE_SUM = "issuesNotCompletedEstimateSum";

    private final JiraAuthorizationService jiraAuthorizationService;

    private final ProjectService projectService;

    public String getJiraAuthorizationLink(Project project) {
        checkJiraUrlConfig(project);

        Optional<String> temporaryToken = projectService.getJiraUrl(project)
                .flatMap(jiraAuthorizationService::getTemporaryToken);

        return temporaryToken.flatMap(token -> {
            projectService.updateRequestToken(project, token);
            return projectService.getJiraUrl(project)
                    .flatMap(url -> jiraAuthorizationService.getVerificationCodeUrl(url, token));
        }).orElseThrow(() -> new NotAcceptableException("Missing Jira key configuration"));
    }

    private void checkJiraUrlConfig(Project project) {
        if (project.getJiraUrl() == null) {
            throw new NotAcceptableException("Missing Jira url configuration");
        }
    }

    public Optional<JiraBoardDto> getJiraBoard(Project project) {
        return getJiraBoard(project, project.getJiraBoardId());
    }

    public Optional<JiraBoardDto> getJiraBoard(Project project, Long jiraBoardId) {
        checkFullJiraConfig(project);
        JSONArray jiraBoards = projectService.getJiraUrl(project).flatMap(url ->
                jiraAuthorizationService.makeAuthorizedGetRequest(url, project.getJiraSecret(),
                        project.getAccessToken(), String.format("%s/%d", JIRA_BOARDS_ENDPOINT, jiraBoardId)))
                .orElseThrow(BadRequestException::new);
        return getJiraBoardDtos(jiraBoards).stream()
                .findFirst();
    }

    private List<JiraBoardDto> getJiraBoardDtos(JSONArray jiraBoards) {
        List<JiraBoardDto> jiraBoardDtos = new LinkedList<>();

        for (int i = 0; i < jiraBoards.length(); i++) {
            JSONObject board = jiraBoards.getJSONObject(i);
            JSONObject boardProjectDetails = board.getJSONObject(LOCATION);

            JiraBoardDto jiraBoardDto = new JiraBoardDto()
                    .boardName(board.getString(NAME))
                    .boardId(board.getLong(ID))
                    .boardType(board.getString(TYPE))
                    .projectId(boardProjectDetails.getLong(PROJECT_ID))
                    .projectKey(boardProjectDetails.getString(PROJECT_KEY))
                    .projectName(boardProjectDetails.getString(PROJECT_NAME));

            jiraBoardDtos.add(jiraBoardDto);
        }
        return jiraBoardDtos;
    }

    public List<JiraBoardDto> getJiraBoards(Project project) {
        checkFullJiraConfig(project);
        JSONArray jiraBoards = projectService.getJiraUrl(project).flatMap(url ->
                jiraAuthorizationService.makeAuthorizedGetRequest(url, project.getJiraSecret(),
                        project.getAccessToken(), JIRA_BOARDS_ENDPOINT))
                .orElseThrow(BadRequestException::new)
                .getJSONObject(0).getJSONArray(VALUES);
        return getJiraBoardDtos(jiraBoards);
    }

    private void checkFullJiraConfig(Project project) {
        checkJiraUrlConfig(project);
        checkJiraRequestTokenConfig(project);
        if (project.getJiraSecret() == null || project.getAccessToken() == null) {
            throw new NotAcceptableException("Missing Jira configuration");
        }
    }

    private void checkJiraRequestTokenConfig(Project project) {
        if (project.getRequestToken() == null) {
            throw new NotAcceptableException("Missing Jira request token configuration");
        }
    }

    public void setJiraSecret(Project project, String jiraSecret) {
        checkJiraUrlConfig(project);
        checkJiraRequestTokenConfig(project);

        projectService.updateJiraSecret(project, jiraSecret);
        String accessToken = projectService.getJiraUrl(project)
                .flatMap(url -> jiraAuthorizationService.getAccessToken(url, jiraSecret, project.getRequestToken()))
                .orElseThrow(() -> new NotAcceptableException("Incorrect Jira configuration"));

        projectService.updateAccessToken(project, accessToken);
    }

    public void setJiraBoard(Project project, JiraBoardDto jiraBoardDto) {
        checkFullJiraConfig(project);

        projectService.updateJiraBoardId(project, jiraBoardDto.getBoardId());
        projectService.updateJiraProjectId(project, jiraBoardDto.getProjectId());
    }

    public List<JiraSprintDto> getAvailableJiraSprints(Sprint sprint) {
        Project project = sprint.getProject();
        JSONArray jiraSprints = projectService.getJiraUrl(project)
                .flatMap(url -> jiraAuthorizationService.makeAuthorizedGetRequest(url, project.getJiraSecret(),
                        project.getAccessToken(), String.format(JIRA_SPRINTS_ENDPOINT, project.getJiraBoardId())))
                .orElseThrow(BadRequestException::new)
                .getJSONObject(0)
                .getJSONArray(VALUES);

        List<JiraSprintDto> jiraSprintDtos = new LinkedList<>();
        for (int i = 0; i < jiraSprints.length(); i++) {
            JSONObject sprintJsonObject = jiraSprints.getJSONObject(i);
            LocalDateTime jiraSprintStart = convertLocalDateTimeFromUtc(sprintJsonObject.getString(START_DATE));
            if (Duration.between(jiraSprintStart, sprint.getDurationPeriod().getStart()).toMinutes()
                    <= MAX_SPRINT_OFFSET) {
                JiraSprintDto jiraSprintDto = new JiraSprintDto()
                        .name(sprintJsonObject.getString(NAME))
                        .id(sprintJsonObject.getLong(ID))
                        .state(sprintJsonObject.getString(STATE))
                        .startDate(jiraSprintStart);
                jiraSprintDtos.add(jiraSprintDto);
            }
        }
        return jiraSprintDtos;
    }

    private LocalDateTime convertLocalDateTimeFromUtc(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public Optional<SprintStats> updateFromJira(Project project, Long jiraSprintId) {
        checkFullJiraConfig(project);
        SprintStats sprintStats = new SprintStats();
        try {
            JSONObject jiraNumbers = projectService.getJiraUrl(project)
                    .flatMap(url -> jiraAuthorizationService.makeAuthorizedGetRequest(url, project.getJiraSecret(),
                            project.getAccessToken(),
                            String.format(JIRA_HOURS_ENDPOINT, jiraSprintId, project.getJiraBoardId())))
                    .orElseThrow(BadRequestException::new)
                    .getJSONObject(0)
                    .getJSONObject(CONTENTS);

            long estimatedPlanned = jiraNumbers.getJSONObject(ISSUES_NOT_COMPLETED_INITIAL_ESTIMATE_SUM).getLong(VALUE);
            long finalPlanned =
                    (jiraNumbers.getJSONObject(ALL_ISSUES_ESTIMATE_SUM).getLong(VALUE) + estimatedPlanned) / 2;
            long burned = finalPlanned - jiraNumbers.getJSONObject(ISSUES_NOT_COMPLETED_ESTIMATE_SUM).getLong(VALUE);

            sprintStats.setEstimatedTimePlanned(Duration.ofSeconds(estimatedPlanned));
            sprintStats.setFinalTimePlanned(Duration.ofSeconds(finalPlanned));
            sprintStats.setTimeBurned(Duration.ofSeconds(burned));
        } catch (JSONException e) {
            return Optional.empty();
        }
        return Optional.of(sprintStats);
    }
}
