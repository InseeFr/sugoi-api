package fr.insee.sugoi.jms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.jms.writer.JmsWriter;

@ActiveProfiles("brokerEmbedded")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JmsBrokerTests {

    @Autowired
    JmsWriter jmsWriter;

    @Autowired
    JmsTemplate jmsTemplate;

    @Test
    public void testSender() throws InterruptedException, JsonMappingException, JsonProcessingException, JMSException {
        // Start Listener
        List<TextMessage> messages = jmsTemplate.browse("queue.Tests", new BrowserCallback<List<TextMessage>>() {
            @Override
            public List<TextMessage> doInJms(Session session, QueueBrowser qb) throws JMSException {
                List<TextMessage> messages = new ArrayList<>();
                Map<String, Object> params = new HashMap<>();
                jmsWriter.writeRequestInQueue("queue.Tests", "toto", params);
                jmsWriter.writeRequestInQueue("queue.Tests2", "tato", params);
                Enumeration<Message> e = qb.getEnumeration();
                while (e.hasMoreElements()) {
                    final Message m = e.nextElement();
                    messages.add((TextMessage) m);
                }
                return messages;
            }
        });
        Assertions.assertEquals(1, messages.size());
        ObjectMapper mapper = new ObjectMapper();
        BrokerRequest requestReceive = mapper.readValue(messages.get(0).getText(), BrokerRequest.class);
        Assertions.assertEquals(requestReceive.getMethod(), "toto");
        Assertions.assertEquals(requestReceive.getmethodParams(), new HashMap<>());
    }

}
