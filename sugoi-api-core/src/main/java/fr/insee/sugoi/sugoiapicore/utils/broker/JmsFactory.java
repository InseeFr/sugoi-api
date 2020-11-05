package fr.insee.sugoi.sugoiapicore.utils.broker;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

public class JmsFactory {

    public static ActiveMQConnectionFactory connectionFactory(String broker_url) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(broker_url);
        return connectionFactory;
    }

    public static JmsTemplate getJmsTemplate(String broker_url) {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory(broker_url));
        template.setMessageConverter(messageConverter());
        return template;
    }

    public static DefaultJmsListenerContainerFactory jmsListenerContainerFactory(String broker_url) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory(broker_url));
        factory.setConcurrency("1-1");
        return factory;
    }

    public static MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
