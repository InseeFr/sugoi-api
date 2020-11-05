package fr.insee.sugoi.services.controller.v2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.sugoiapicore.service.broker.BrokerRequest;
import fr.insee.sugoi.sugoiapicore.utils.broker.JmsFactory;

@RestController("/broker")
public class BrokerController {

    @Value("${fr.insee.sugoi.broker.url}")
    private String url;

    @PostMapping(value = "/post")
    public String getMethodName() {
        JmsTemplate jmsProducer = JmsFactory.getJmsTemplate(url);
        BrokerRequest request = new BrokerRequest();
        request.setMethod("Post");
        request.setRequestBody("trololol");
        jmsProducer.convertAndSend("toto", request);
        return "done";
    }

}
