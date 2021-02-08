package fr.insee.sugoi.event.listener.webhook.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import fr.insee.sugoi.event.listener.webhook.service.WebHookService;

@Service
public class WebHookServiceImpl implements WebHookService {

    public static final Logger logger = LoggerFactory.getLogger(WebHookService.class);

    @Autowired
    private Environment env;

    @Override
    public void send(String webHookName, Map<String, String> values) {
        String content = env.getProperty("sugoi.api.event.webhook." + webHookName + ".content");
        String target = env.getProperty("sugoi.api.event.webhook." + webHookName + ".target");
        String authType = env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.type");
        try {
            content = StringSubstitutor.replace(content, values, "$(", ")");
            Map<String, String> headers = new HashMap<>();
            headers.put("content-type", "application/json");
            if (authType.equalsIgnoreCase("basic")) {
                String username = env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.user");
                String password = env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.password");

                String auth = username + ":" + password;
                byte[] encodedAuth = Base64Utils.encode(auth.getBytes());
                String authHeader = "Basic " + new String(encodedAuth);
                headers.put("Authorization", authHeader);
            } else if (authType.equalsIgnoreCase("oauth")) {
                String token = env
                        .getProperty(env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.token"));
                headers.put("Authorization", "bearer " + token);
            }
            HttpHeaders _headers = new HttpHeaders();
            headers.keySet().stream().forEach(header -> _headers.add(header, headers.get(header)));
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(content, _headers);
            ResponseEntity<String> response = restTemplate.postForEntity(target, request, String.class);
            if (response.getStatusCode().value() >= 400) {
                logger.info("Something went wrong when sending request to {} receive status {} response {}", target,
                        response.getStatusCode().value(), response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong when sending webhook to target " + target, e);
        }

    }

}
