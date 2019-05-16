package pl.edu.agh.papaya.tests.util;

import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.client.ApiClient;
import pl.edu.agh.papaya.api.client.service.ProjectsApi;

/**
 * Provides API beans to be used in fixtures.
 */
@Component
public class ClientApiProvider {

    private ApiClient apiClient;

    @LocalServerPort
    private int port;

    public ApiClient getApiClient() {
        if (apiClient == null) {
            apiClient = new DescriptiveApiClient()
                    .setBasePath("http://localhost:" + port + "/api");
        }

        return apiClient;
    }

    @Bean
    public ProjectsApi projectsApi() {
        return new ProjectsApi(getApiClient());
    }
}
