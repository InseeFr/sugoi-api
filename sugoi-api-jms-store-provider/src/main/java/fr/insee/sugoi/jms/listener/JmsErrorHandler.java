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
package fr.insee.sugoi.jms.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.adapter.ListenerExecutionFailedException;
import org.springframework.util.ErrorHandler;

/**
 * Custom error handler for JMS message listener failures.
 * Logs errors and prevents them from being silently ignored.
 */
public class JmsErrorHandler implements ErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(JmsErrorHandler.class);

  @Override
  public void handleError(Throwable t) {
    logger.error("JMS Listener error: {}", t.getMessage(), t);
    
    // Log the root cause if available
    if (t.getCause() != null) {
      logger.error("Root cause: {}", t.getCause().getMessage(), t.getCause());
    }
  }
}
