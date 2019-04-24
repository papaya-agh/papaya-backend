package pl.edu.agh.papaya.tests.util;

import pl.edu.agh.papaya.api.client.ApiException;
import pl.edu.agh.papaya.api.client.ApiResponse;

public class ConcordionTestUtils {

    public <T> ApiResponse<T> failSilently(ApiRequest<T> request) {
        try {
            return request.request();
        } catch (ApiException e) {
            return new ApiResponse<>(e.getCode(), e.getResponseHeaders());
        }
    }
}
