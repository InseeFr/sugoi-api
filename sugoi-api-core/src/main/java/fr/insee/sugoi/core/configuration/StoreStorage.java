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
package fr.insee.sugoi.core.configuration;

import fr.insee.sugoi.core.store.Store;
import java.util.Map;

/**
 * Store configuration from realm Each realm declares a writerType and a readerType (could be null).
 *
 * <p>Default implementation fetch beans from the name of the type
 *
 * @see StoreStorageImpl for details
 */
public interface StoreStorage {

  public Store getStore(String readerType, String writerType, Map<String, String> config);
}
