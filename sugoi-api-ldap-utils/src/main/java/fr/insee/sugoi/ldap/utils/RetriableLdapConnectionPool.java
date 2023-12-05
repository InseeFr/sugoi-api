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
package fr.insee.sugoi.ldap.utils;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetriableLdapConnectionPool {

  private final int maxRetries;
  private final LDAPConnectionPool ldapConnectionPool;
  public static final Logger logger = LoggerFactory.getLogger(RetriableLdapConnectionPool.class);

  public LDAPConnectionPool getLdapConnectionPool() {
    return ldapConnectionPool;
  }

  public RetriableLdapConnectionPool(LDAPConnection connection, int numConnections, int maxRetries)
      throws LDAPException {
    this.ldapConnectionPool = new LDAPConnectionPool(connection, numConnections);
    this.maxRetries = maxRetries;
  }

  public SearchResultEntry getEntry(String dn, String... attributes) throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.getEntry(dn, attributes);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to retrieve entry after " + maxRetries + " retries.");
  }

  public SearchResult search(SearchRequest searchRequest) throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.search(searchRequest);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to find entry after " + maxRetries + " retries.");
  }

  public SearchResult search(String baseDN, SearchScope scope, Filter filter)
      throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.search(baseDN, scope, filter);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to find entry after " + maxRetries + " retries.");
  }

  public LDAPResult add(AddRequest addRequest) throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.add(addRequest);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to add entry after " + maxRetries + " retries.");
  }

  public LDAPResult modify(ModifyRequest modifyRequest)
      throws LDAPException, RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.modify(modifyRequest);
      } catch (LDAPException e) {
        if (e.getResultCode().equals(ResultCode.NO_SUCH_ATTRIBUTE)
            || e.getResultCode().equals(ResultCode.ATTRIBUTE_OR_VALUE_EXISTS)) {
          throw e;
        }
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to modify entry after " + maxRetries + " retries.");
  }

  public LDAPResult modify(String dn, Modification modification) throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.modify(dn, modification);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to modify entry after " + maxRetries + " retries.");
  }

  public LDAPResult delete(DeleteRequest deleteRequest) throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.delete(deleteRequest);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to delete entry after " + maxRetries + " retries.");
  }

  public LDAPResult delete(String dn) throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.delete(dn);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to delete entry after " + maxRetries + " retries.");
  }

  public ExtendedResult processExtendedOperation(PasswordModifyExtendedRequest pmer)
      throws RetriableLDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnectionPool.processExtendedOperation(pmer);
      } catch (LDAPException e) {
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException(
        "Failed to process extended operation after " + maxRetries + " retries.");
  }

  public void close() {
    ldapConnectionPool.close();
  }
}
