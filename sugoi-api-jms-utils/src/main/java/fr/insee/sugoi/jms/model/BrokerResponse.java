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
package fr.insee.sugoi.jms.model;

import java.util.UUID;

import javax.jms.JMSException;

import org.apache.activemq.command.Message;
import org.apache.activemq.command.Response;
import org.apache.activemq.state.CommandVisitor;

import fr.insee.sugoi.core.model.ProviderResponse;

/** BrokerResponse */
public class BrokerResponse extends Message {

  private String comment;

  private ProviderResponse providerResponse;

  private UUID uuid;

  public BrokerResponse() {
    uuid = UUID.randomUUID();
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public ProviderResponse getProviderResponse() {
    return providerResponse;
  }

  public void setProviderResponse(ProviderResponse providerResponse) {
    this.providerResponse = providerResponse;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public String toString() {
    return "{uuid: " + uuid.toString() + " comment: " + comment + "}";
  }

  @Override
  public Response visit(CommandVisitor visitor) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte getDataStructureType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Message copy() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearBody() throws JMSException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeContentAndClear() {
    throw new UnsupportedOperationException();
  }
}
