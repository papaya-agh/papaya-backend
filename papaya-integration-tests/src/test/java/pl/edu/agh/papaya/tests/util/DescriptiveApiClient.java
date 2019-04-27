package pl.edu.agh.papaya.tests.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import java.io.IOException;
import java.lang.reflect.Type;
import pl.edu.agh.papaya.api.client.ApiClient;
import pl.edu.agh.papaya.api.client.ApiException;
import pl.edu.agh.papaya.api.client.ApiResponse;

/**
 * {@link ApiClient} by default somehow <em>does not</em> add a message to thrown
 * {@link ApiException}s, this class fixes that.
 */
public class DescriptiveApiClient extends ApiClient {

    @Override
    public <T> ApiResponse<T> execute(Call call, Type returnType) throws ApiException {
        try {
            return super.execute(call, returnType);
        } catch (ApiException e) {
            throw describe(e);
        }
    }

    private ApiException describe(ApiException e) {
        return new ApiException(createMessage(e), e.getCode(), e.getResponseHeaders(), e.getResponseBody());
    }

    private String createMessage(ApiException apiException) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object response = mapper.readValue(apiException.getResponseBody(), Object.class);
            String prettyPrintResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            return "Response code: " + apiException.getCode() + ", response:\n" + prettyPrintResponse;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
