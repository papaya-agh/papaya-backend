package pl.edu.agh.papaya.util;

import org.json.JSONArray;
import org.json.JSONObject;

public final class JiraUtil {

    public static final int MAX_SPRINT_OFFSET = 60 * 24 * 30;
    public static final String JIRA_HOURS_ENDPOINT =
            "/rest/greenhopper/1.0/rapid/charts/sprintreport?sprintId=%d&rapidViewId=%d";
    public static final String JIRA_BOARDS_ENDPOINT = "/rest/agile/1.0/board";
    public static final String JIRA_SPRINTS_ENDPOINT = "/rest/agile/1.0/board/%d/sprint";
    public static final String STATE = "state";
    public static final String START_DATE = "startDate";
    public static final String CONTENTS = "contents";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_KEY = "projectKey";
    public static final String PROJECT_NAME = "projectName";
    public static final String LOCATION = "location";
    public static final String ISSUES_NOT_COMPLETED_INITIAL_ESTIMATE_SUM = "issuesNotCompletedInitialEstimateSum";
    public static final String ALL_ISSUES_ESTIMATE_SUM = "allIssuesEstimateSum";
    public static final String ISSUES_NOT_COMPLETED_IN_CURRENT_SPRINT = "issuesNotCompletedInCurrentSprint";
    public static final String CURRENT_ESTIMATE_STATISTIC = "currentEstimateStatistic";
    public static final String STAT_FIELD_VALUE = "statFieldValue";
    public static final String TRACKING_STATISTIC = "trackingStatistic";
    public static final String COMPLETED_ISSUES = "completedIssues";

    private JiraUtil() {
    }

    public static long getTotalTimeBurned(JSONObject jiraNumbers) {
        long timeBurnedInCompleted = getTimeBurned(jiraNumbers, COMPLETED_ISSUES);
        long timeBurnedInNotCompleted = getTimeBurned(jiraNumbers, ISSUES_NOT_COMPLETED_IN_CURRENT_SPRINT);
        return timeBurnedInCompleted + timeBurnedInNotCompleted;
    }

    public static long getTimeBurned(JSONObject jiraNumbers, String issuesNotCompletedInCurrentSprint) {
        long timeBurned = 0;
        JSONArray issuesNotCompleted = jiraNumbers.getJSONArray(issuesNotCompletedInCurrentSprint);
        for (int i = 0; i < issuesNotCompleted.length(); i++) {
            long issueTime = issuesNotCompleted.getJSONObject(i).getJSONObject(CURRENT_ESTIMATE_STATISTIC)
                    .getJSONObject(STAT_FIELD_VALUE)
                    .getLong(VALUE);
            long issueTimeRemaining = issuesNotCompleted.getJSONObject(i).getJSONObject(TRACKING_STATISTIC)
                    .getJSONObject(STAT_FIELD_VALUE)
                    .getLong(VALUE);
            timeBurned += issueTime - issueTimeRemaining;
        }
        return timeBurned;
    }

    public static long getEstimatedPlanned(JSONObject jiraNumbers) {
        return jiraNumbers.getJSONObject(ISSUES_NOT_COMPLETED_INITIAL_ESTIMATE_SUM).getLong(VALUE);
    }

    public static long getFinalPlanned(JSONObject jiraNumbers, long estimatedPlanned) {
        return (jiraNumbers.getJSONObject(JiraUtil.ALL_ISSUES_ESTIMATE_SUM)
                .getLong(JiraUtil.VALUE) + estimatedPlanned) / 2;
    }
}
