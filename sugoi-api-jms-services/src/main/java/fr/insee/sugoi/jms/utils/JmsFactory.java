/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.jms.utils;

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
