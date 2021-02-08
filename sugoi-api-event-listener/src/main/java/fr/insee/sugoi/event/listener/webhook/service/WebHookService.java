package fr.insee.sugoi.event.listener.webhook.service;

import java.util.Map;

public interface WebHookService {

    void send(String webHookName, Map<String, String> value);

}
