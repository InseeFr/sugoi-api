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
package fr.insee.sugoi.jms.configuration;

import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/** ConsumerConfiguration */
@Configuration
@EnableJms
public class JmsConfiguration {

  private static final Logger logger = LogManager.getLogger(JmsConfiguration.class);

  @Value("${fr.insee.sugoi.jms.broker.url}")
  private String url;

  @Value("${fr.insee.sugoi.jms.broker.username:}")
  private String username;

  @Value("${fr.insee.sugoi.jms.broker.password:}")
  private String password;

  @Bean
  public ActiveMQConnectionFactory connectionFactory() {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
    logger.info("Configure jms connection for uri: {}", url);
    connectionFactory.setBrokerURL(url);
    if (username != null) {
      connectionFactory.setUserName(username);
    }
    if (password != null) {
      connectionFactory.setPassword(password);
    }

    return connectionFactory;
  }

  @Bean
  public JmsTemplate getJmsTemplate() {
    JmsTemplate template = new JmsTemplate();
    template.setConnectionFactory(connectionFactory());
    template.setMessageConverter(messageConverter());
    return template;
  }

  @Bean
  public JmsListenerContainerFactory<?> myFactory(
      ConnectionFactory connectionFactory,
      DefaultJmsListenerContainerFactoryConfigurer configurer) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    // This provides all boot's default to this factory, including the message
    // converter
    configurer.configure(factory, connectionFactory);
    // You could still override some of Boot's default if necessary.
    return factory;
  }

  @Bean
  public static MessageConverter messageConverter() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    return converter;
  }
}
