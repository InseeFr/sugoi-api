package fr.insee.sugoi.sugoiapicore.dao.broker;

import java.util.Map;

import org.springframework.jms.core.JmsTemplate;

import fr.insee.sugoi.model.Technique.WriterStore;
import fr.insee.sugoi.sugoiapicore.utils.broker.JmsFactory;

public class BrokerWriterStore implements WriterStore {

    private JmsTemplate jmsTemplate;

    public BrokerWriterStore(Map<String, String> config) {
        jmsTemplate = JmsFactory.getJmsTemplate(config.get("url"));
    }

    @Override
    public String deleteUser(String domain, String id) {
        jmsTemplate.convertAndSend("destination", "message");
        return null;
    }

}
