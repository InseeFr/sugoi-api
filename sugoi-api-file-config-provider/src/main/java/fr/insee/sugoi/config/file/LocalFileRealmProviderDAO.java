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
package fr.insee.sugoi.config.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.configuration.RealmProvider;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.Realm;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "fr.insee.sugoi.realm.config.type", havingValue = "local", matchIfMissing = true)
public class LocalFileRealmProviderDAO implements RealmProvider {

    @Value("${fr.insee.sugoi.realm.config.local.path:/realms.json}")
    private String localFilePath;

    private List<Realm> realms;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Realm load(String realmName) {
        if (realms == null) {
            try {
                URL is = this.getClass().getResource(localFilePath);
                realms = mapper.readValue(is, new TypeReference<List<Realm>>() {
                });
            } catch (IOException e) {
                throw new RealmNotFoundException(e.getMessage());
            }
        }
        return realms.stream().filter(r -> r.getName().equalsIgnoreCase(realmName)).findFirst().orElse(null);
    }

    @Override
    public List<Realm> findAll() {
        try {
            URL is = this.getClass().getResource(localFilePath);
            realms = mapper.readValue(is, new TypeReference<List<Realm>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realms;
    }
}
