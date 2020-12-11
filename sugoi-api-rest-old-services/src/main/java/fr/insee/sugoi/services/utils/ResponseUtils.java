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
package fr.insee.sugoi.services.utils;

import fr.insee.sugoi.core.model.PageResult;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ResponseUtils {

  private static <T> URI fetchNextResultUri(PageResult<T> page) {
    UriComponentsBuilder uriComponents =
        UriComponentsBuilder.newInstance().scheme("http").host("www.google.com").path("/");
    if (page.isHasMoreResult()) {
      if (page.getSearchCookie() != null) {}

      if (page.getNextStart() > 0) {
        uriComponents = uriComponents.queryParam("start", page.getNextStart());
      }
    }
    return uriComponents.build().toUri();
  }

  public static HttpHeaders ajouterHeadersTotalSizeEtNextLocation(
      PageResult<?> page, boolean premiereRecherche, int sizeDemande) {
    HttpHeaders headers = new HttpHeaders();
    // Try to get total result size, return -1 if cannot
    if (page.getPageSize() == -1 && premiereRecherche && page.getResults().size() < sizeDemande) {
      headers.add("X-Total-Size", Integer.toString(page.getResults().size()));
    } else {
      headers.add("X-Total-Size", Integer.toString(page.getTotalElements()));
    }

    // Add nextLocation if needed
    if (page.isHasMoreResult()) {
      URI uri = fetchNextResultUri(page);
      headers.add("nextLocation", uri.toString());
    }
    return headers;
  }
}
