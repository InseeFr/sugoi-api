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
package fr.insee.sugoi.core.model;

public class ProviderResponse {

  ProviderResponseStatus status;
  String RequestId;
  String EntityId;
  RuntimeException exception;

  public enum ProviderResponseStatus {
    // Request is executed and result is effective at once
    OK,
    // Request has failed
    KO,
    // Request is registered but could eventually be refused or fail
    REQUESTED,
    // Request is executed with success but could be effective later on
    ACCEPTED;
  }

  public ProviderResponseStatus getStatus() {
    return status;
  }

  public void setStatus(ProviderResponseStatus status) {
    this.status = status;
  }

  public String getEntityId() {
    return EntityId;
  }

  public void setEntityId(String entityId) {
    EntityId = entityId;
  }

  public String getRequestId() {
    return RequestId;
  }

  public void setRequestId(String requestId) {
    RequestId = requestId;
  }

  public RuntimeException getException() {
    return exception;
  }

  public void setException(RuntimeException exception) {
    this.exception = exception;
  }
}
