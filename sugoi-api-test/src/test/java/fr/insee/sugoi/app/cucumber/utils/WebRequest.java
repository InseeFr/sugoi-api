/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.app.cucumber.utils;

import fr.insee.sugoi.app.cucumber.configuration.RestTemplateConfiguration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class WebRequest {

  private RestTemplate restTemplate;

  private static final String BASE_URL = "http://localhost:8080";

  public WebRequest() {
    this.restTemplate = RestTemplateConfiguration.getRestTemplate();
  }

  public ResponseResults executeGet(String url, Map<String, String> headers, String body)
      throws IOException {
    final Map<String, String> _headers = new HashMap<>();
    _headers.put("Accept", "application/json");
    _headers.put("Content-Type", "application/json");
    if (headers != null) {
      headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
    }
    final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
    final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

    restTemplate.setErrorHandler(errorHandler);
    return restTemplate.execute(
        BASE_URL + url,
        HttpMethod.GET,
        requestCallback,
        response -> {
          if (errorHandler.hadError) {
            return (errorHandler.getResults());
          } else {
            return (new ResponseResults(response));
          }
        });
  }

  public ResponseResults executeDelete(String url, Map<String, String> headers, String body)
      throws IOException {

    final Map<String, String> _headers = new HashMap<>();
    _headers.put("Accept", "application/json");
    _headers.put("Content-Type", "application/json");

    if (headers != null) {
      headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
    }
    final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
    final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

    restTemplate.setErrorHandler(errorHandler);
    return restTemplate.execute(
        BASE_URL + url,
        HttpMethod.DELETE,
        requestCallback,
        response -> {
          if (errorHandler.hadError) {
            return (errorHandler.getResults());
          } else {
            return (new ResponseResults(response));
          }
        });
  }

  public ResponseResults executeUpdate(String url, Map<String, String> headers, String body)
      throws IOException {

    final Map<String, String> _headers = new HashMap<>();
    _headers.put("Accept", "application/json");
    _headers.put("Content-Type", "application/json");

    if (headers != null) {
      headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
    }
    final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
    final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

    restTemplate.setErrorHandler(errorHandler);
    return restTemplate.execute(
        BASE_URL + url,
        HttpMethod.PUT,
        requestCallback,
        response -> {
          if (errorHandler.hadError) {
            return (errorHandler.getResults());
          } else {
            return (new ResponseResults(response));
          }
        });
  }

  public ResponseResults executePost(String url, Map<String, String> headers, String body)
      throws IOException {

    final Map<String, String> _headers = new HashMap<>();
    _headers.put("Accept", "application/json");
    _headers.put("Content-Type", "application/json");

    if (headers != null) {
      headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
    }

    final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
    final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

    restTemplate.setErrorHandler(errorHandler);
    return restTemplate.execute(
        BASE_URL + url,
        HttpMethod.POST,
        requestCallback,
        response -> {
          if (errorHandler.hadError) {
            return (errorHandler.getResults());
          } else {
            return (new ResponseResults(response));
          }
        });
  }

  public ResponseResults executePatch(String url, Map<String, String> headers, String body)
      throws IOException {

    final Map<String, String> _headers = new HashMap<>();
    _headers.put("Accept", "application/json");
    _headers.put("Content-Type", "application/json");
    if (headers != null) {
      headers.keySet().stream().forEach(key -> _headers.put(key, headers.get(key)));
    }
    final MyRequestCallback requestCallback = new MyRequestCallback(_headers, body);
    final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

    restTemplate.setErrorHandler(errorHandler);
    return restTemplate.execute(
        BASE_URL + url,
        HttpMethod.PATCH,
        requestCallback,
        response -> {
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
