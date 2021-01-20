package fr.insee.sugoi.app.cucumber.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class WebRequest {

    RestTemplate restTemplate = new RestTemplate();

    private static final String BASE_URL = "http://localhost:8080";

    public ResponseResults executeGet(String url, Map<String, String> headers, String body) throws IOException {
        final Map<String, String> _headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
        final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        restTemplate.setErrorHandler(errorHandler);
        return restTemplate.execute(BASE_URL + url, HttpMethod.GET, requestCallback, response -> {
            if (errorHandler.hadError) {
                return (errorHandler.getResults());
            } else {
                return (new ResponseResults(response));
            }
        });
    }

    public ResponseResults executeDelete(String url, Map<String, String> headers, String body) throws IOException {
        final Map<String, String> _headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
        final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        restTemplate.setErrorHandler(errorHandler);
        return restTemplate.execute(BASE_URL + url, HttpMethod.DELETE, requestCallback, response -> {
            if (errorHandler.hadError) {
                return (errorHandler.getResults());
            } else {
                return (new ResponseResults(response));
            }
        });
    }

    public ResponseResults executeUpdate(String url, Map<String, String> headers, String body) throws IOException {
        final Map<String, String> _headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
        final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        restTemplate.setErrorHandler(errorHandler);
        return restTemplate.execute(BASE_URL + url, HttpMethod.PUT, requestCallback, response -> {
            if (errorHandler.hadError) {
                return (errorHandler.getResults());
            } else {
                return (new ResponseResults(response));
            }
        });
    }

    public ResponseResults executePost(String url, Map<String, String> headers, String body) throws IOException {
        final Map<String, String> _headers = new HashMap<>();
        _headers.put("Accept", "application/json");
        headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
        final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }

        restTemplate.setErrorHandler(errorHandler);
        return restTemplate.execute(BASE_URL + url, HttpMethod.POST, requestCallback, response -> {
            if (errorHandler.hadError) {
                return (errorHandler.getResults());
            } else {
                return (new ResponseResults(response));
            }
        });
    }

    private class ResponseResultErrorHandler implements ResponseErrorHandler {
        private ResponseResults results = null;
        private Boolean hadError = false;

        private ResponseResults getResults() {
            return results;
        }

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            hadError = response.getRawStatusCode() >= 400;
            return hadError;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            results = new ResponseResults(response);
        }
    }

}
