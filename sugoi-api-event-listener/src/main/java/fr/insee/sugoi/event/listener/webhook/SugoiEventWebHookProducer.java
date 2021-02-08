package fr.insee.sugoi.event.listener.webhook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.core.event.model.SugoiEvent;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.event.listener.webhook.service.WebHookService;
import fr.insee.sugoi.model.User;

@Component
public class SugoiEventWebHookProducer {

    @Value("${sugoi.api.event.webhook.name}")
    private List<String> webHookNames;

    @Autowired
    private WebHookService webHookService;

    @EventListener
    public void handleContextStart(SugoiEvent cse) {
        SugoiEventTypeEnum eventType = cse.getEventType();
        switch (eventType) {
            case CHANGE_PASSWORD:

                break;
            case INIT_PASSWORD:

                break;
            case RESET_PASSWORD:
                Map<String, String> values = new HashMap<>();
                User user = (User) cse.getProperties().get("user");
                String password = (String) cse.getProperties().get("password");
                PasswordChangeRequest pcr = (PasswordChangeRequest) cse.getProperties().get("pcr");
                values.put("userID", user.getUsername());
                values.put("mail", pcr.getEmail() != null ? pcr.getEmail() : user.getMail());
                values.put("password", password);
                webHookNames.stream().forEach(webHookName -> webHookService.send(webHookName, values));
                break;
            default:
                break;
        }
    }

}
