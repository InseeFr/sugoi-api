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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.RealmAlreadyExistException;
import fr.insee.sugoi.core.exceptions.RealmNotCreatedException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** ConfigServiceImpl */
@Service
public class ConfigServiceImpl implements ConfigService {

  @Autowired private RealmProvider realmProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Override
  public Optional<Realm> getRealm(String name) {
    try {
      Realm realm = realmProvider.load(name);
      sugoiEventPublisher.publishCustomEvent(
          null,
          null,
          SugoiEventTypeEnum.FIND_REALM_BY_ID,
          Map.ofEntries(Map.entry(EventKeysConfig.REALM_NAME, name)));
      return Optional.ofNullable(realm);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          null,
          null,
          SugoiEventTypeEnum.FIND_REALM_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.REALM_NAME, name),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      return Optional.empty();
    }
  }

  @Override
  public List<Realm> getRealms() {
    try {
      List<Realm> realms = realmProvider.findAll();
      sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.FIND_REALMS, null);
      return realms;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          null,
          null,
          SugoiEventTypeEnum.FIND_REALMS_ERROR,
          Map.ofEntries(Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public void deleteRealm(String realmName) {
    try {
      getRealm(realmName)
          .orElseThrow(() -> new RealmNotFoundException("Realm " + realmName + " not found"));
      realmProvider.deleteRealm(realmName);
      sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.DELETE_REALM, null);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          null,
          null,
          SugoiEventTypeEnum.DELETE_REALM_ERROR,
          Map.ofEntries(Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof RealmNotFoundException) {
        throw (RealmNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void updateRealm(Realm realm) {
    try {
      getRealm(realm.getName())
          .orElseThrow(() -> new RealmNotFoundException("Realm " + realm.getName() + " not found"));
      realmProvider.updateRealm(realm);
      sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.UPDATE_REALM, null);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          null,
          null,
          SugoiEventTypeEnum.UPDATE_REALM_ERROR,
          Map.ofEntries(Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof RealmNotFoundException) {
        throw (RealmNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void createRealm(Realm realm) {
    try {
      if (getRealm(realm.getName()).isEmpty()) {
        realmProvider.createRealm(realm);
        sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.CREATE_REALM, null);
      }
      throw new RealmAlreadyExistException("Realm " + realm.getName() + "already exist");
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          null,
          null,
          SugoiEventTypeEnum.CREATE_REALM_ERROR,
          Map.ofEntries(Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof RealmAlreadyExistException) {
        throw (RealmAlreadyExistException) e;
      } else if (e instanceof RealmNotCreatedException) {
        throw (RealmNotCreatedException) e;
      } else {
        throw e;
      }
    }
  }
}
