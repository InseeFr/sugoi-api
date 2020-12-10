package fr.insee.sugoi.jms.writer;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.jms.exception.BrokerException;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.jms.model.BrokerResponse;

@Component
public class JmsWriter {

    private static final Logger logger = LogManager.getLogger(JmsWriter.class);

    @Autowired
    JmsTemplate jmsTemplate;

    public void writeRequestInQueue(String queueName, String methodName, Map<String, Object> methodParams) {
        BrokerRequest request = new BrokerRequest();
        request.setMethod(methodName);
        for (String key : new ArrayList<>(methodParams.keySet())) {
            request.setmethodParams(key, methodParams.get(key));
        }
        try {
            jmsTemplate.convertAndSend(queueName, request);
            logger.info("Message send in queue {}", queueName);
        } catch (JmsException e) {
            logger.info("Error when sending message to broker");
            throw new BrokerException(e.getMessage());
        }
    }

    public void writeResponseInQueue(String queueName, String comment, Object object) {
        BrokerResponse response = new BrokerResponse();
        response.setComment(comment);
        response.setObject(object);
        try {
            jmsTemplate.convertAndSend(queueName, response);
            logger.info("Message send in queue {}", queueName);
        } catch (JmsException e) {
            logger.info("Error when sending message to broker");
            throw new BrokerException(e.getMessage());
        }
    }
}
