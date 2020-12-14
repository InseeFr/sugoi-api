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
package fr.insee.sugoi.jms;

import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.jms.utils.JmsFactory;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import java.util.Map;
import org.springframework.jms.core.JmsTemplate;

public class BrokerWriterStore implements WriterStore {

  private JmsTemplate jmsTemplate;

  public BrokerWriterStore(Map<String, String> config) {
    jmsTemplate = JmsFactory.getJmsTemplate(config.get("url"));
  }

  @Override
  public void deleteUser(String id) {
    jmsTemplate.convertAndSend("destination", "message");
  }

  @Override
  public User createUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User updateUser(User updatedUser) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteGroup(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Group createGroup(Group group) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group updateGroup(Group updatedGroup) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteOrganization(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Group createOrganization(Group group) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group updateOrganization(Group updatedGroup) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteUserFromGroup(String groupName, String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addUserToGroup(String groupName, String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void reinitPassword(User user) {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPassword(User user, String password) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    // TODO Auto-generated method stub

  }
}
