package fr.insee.sugoi.app.cucumber.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyRequestCallback implements RequestCallback {

    Map<String, String> requestHeaders = new HashMap<>();

    private String body;

    public MyRequestCallback() {
    }

    public MyRequestCallback(final Map<String, String> headers, final String body) {
        this.body = body;
        this.requestHeaders = headers;
    }

    public void setBody(final String postBody) {
        this.body = postBody;
    }

    public void addRequestHeader(String key, String value) {
        this.requestHeaders.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public void doWithRequest(ClientHttpRequest request) throws IOException {
        final HttpHeaders clientHeaders = request.getHeaders();
        for (final Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            clientHeaders.add(entry.getKey(), entry.getValue());
        }
        if (null != body) {
            request.getBody().write(body.getBytes());
        }
    }
}
