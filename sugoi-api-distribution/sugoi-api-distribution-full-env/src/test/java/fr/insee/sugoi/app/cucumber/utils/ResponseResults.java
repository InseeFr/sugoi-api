package fr.insee.sugoi.app.cucumber.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;

public class ResponseResults {
    private final ClientHttpResponse theResponse;

    private final String body;

    ResponseResults(final ClientHttpResponse response) throws IOException {
        this.theResponse = response;
        final InputStream bodyInputStream = response.getBody();
        this.body = IOUtils.toString(bodyInputStream, StandardCharsets.UTF_8.name());
    }

    ClientHttpResponse getTheResponse() {
        return theResponse;
    }

    public ClientHttpResponse getClientHttpResponse() {
        return this.theResponse;
    }

    public String getBody() {
        return body;
    }
}
