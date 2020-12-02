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
package fr.insee.sugoi.store.file;

import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.User;
import java.util.Map;

public class FileWriterStore implements WriterStore {

  public FileWriterStore(Map<String, String> generateConfig) {}

  @Override
  public String deleteUser(String domain, String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User createUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }
}
