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
import pl.edu.agh.papaya.util.JiraUtil;
import pl.edu.agh.papaya.util.NotAcceptableException;

@Service
@RequiredArgsConstructor
public class JiraRestService {

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
                        project.getAccessToken(), String.format("%s/%d", JiraUtil.JIRA_BOARDS_ENDPOINT, jiraBoardId)))
                .orElseThrow(BadRequestException::new);
        return getJiraBoardDtos(jiraBoards).stream()
                .findFirst();
    }

    private List<JiraBoardDto> getJiraBoardDtos(JSONArray jiraBoards) {
        List<JiraBoardDto> jiraBoardDtos = new LinkedList<>();

        for (int i = 0; i < jiraBoards.length(); i++) {
            JSONObject board = jiraBoards.getJSONObject(i);
            JSONObject boardProjectDetails = board.getJSONObject(JiraUtil.LOCATION);

            JiraBoardDto jiraBoardDto = new JiraBoardDto()
                    .boardName(board.getString(JiraUtil.NAME))
                    .boardId(board.getLong(JiraUtil.ID))
                    .boardType(board.getString(JiraUtil.TYPE))
                    .projectId(boardProjectDetails.getLong(JiraUtil.PROJECT_ID))
                    .projectKey(boardProjectDetails.getString(JiraUtil.PROJECT_KEY))
                    .projectName(boardProjectDetails.getString(JiraUtil.PROJECT_NAME));

            jiraBoardDtos.add(jiraBoardDto);
        }
        return jiraBoardDtos;
    }

    public List<JiraBoardDto> getJiraBoards(Project project) {
        checkFullJiraConfig(project);
        JSONArray jiraBoards = projectService.getJiraUrl(project).flatMap(url ->
                jiraAuthorizationService.makeAuthorizedGetRequest(url, project.getJiraSecret(),
                        project.getAccessToken(), JiraUtil.JIRA_BOARDS_ENDPOINT))
                .orElseThrow(BadRequestException::new)
                .getJSONObject(0).getJSONArray(JiraUtil.VALUES);
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
                        project.getAccessToken(),
                        String.format(JiraUtil.JIRA_SPRINTS_ENDPOINT, project.getJiraBoardId())))
                .orElseThrow(BadRequestException::new)
                .getJSONObject(0)
                .getJSONArray(JiraUtil.VALUES);

        List<JiraSprintDto> jiraSprintDtos = new LinkedList<>();
        for (int i = 0; i < jiraSprints.length(); i++) {
            JSONObject sprintJsonObject = jiraSprints.getJSONObject(i);
            LocalDateTime jiraSprintStart =
                    convertLocalDateTimeFromUtc(sprintJsonObject.getString(JiraUtil.START_DATE));
            if (Duration.between(jiraSprintStart, sprint.getDurationPeriod().getStart()).toMinutes()
                    <= JiraUtil.MAX_SPRINT_OFFSET) {
                JiraSprintDto jiraSprintDto = getJiraSprintDto(sprintJsonObject, jiraSprintStart);
                jiraSprintDtos.add(jiraSprintDto);
            }
        }
        return jiraSprintDtos;
    }

    private JiraSprintDto getJiraSprintDto(JSONObject sprintJsonObject, LocalDateTime jiraSprintStart) {
        return new JiraSprintDto()
                .name(sprintJsonObject.getString(JiraUtil.NAME))
                .id(sprintJsonObject.getLong(JiraUtil.ID))
                .state(sprintJsonObject.getString(JiraUtil.STATE))
                .startDate(jiraSprintStart);
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
                            String.format(JiraUtil.JIRA_HOURS_ENDPOINT, jiraSprintId, project.getJiraBoardId())))
                    .orElseThrow(BadRequestException::new)
                    .getJSONObject(0)
                    .getJSONObject(JiraUtil.CONTENTS);

            long estimatedPlanned = JiraUtil.getEstimatedPlanned(jiraNumbers);
            long finalPlanned = JiraUtil.getFinalPlanned(jiraNumbers, estimatedPlanned);
            long burned = JiraUtil.getTotalTimeBurned(jiraNumbers);

            sprintStats.setEstimatedTimePlanned(Duration.ofSeconds(estimatedPlanned));
            sprintStats.setFinalTimePlanned(Duration.ofSeconds(finalPlanned));
            sprintStats.setTimeBurned(Duration.ofSeconds(burned));
        } catch (JSONException e) {
            return Optional.empty();
        }
        return Optional.of(sprintStats);
    }
}
