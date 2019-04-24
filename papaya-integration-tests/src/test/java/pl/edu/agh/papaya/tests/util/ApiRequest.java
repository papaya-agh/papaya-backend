package pl.edu.agh.papaya.tests.util;

import pl.edu.agh.papaya.api.client.ApiException;
import pl.edu.agh.papaya.api.client.ApiResponse;

public interface ApiRequest<T> {

    ApiResponse<T> request() throws ApiException;
}
