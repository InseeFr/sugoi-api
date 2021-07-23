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
package fr.insee.sugoi.app.cucumber.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;

public class ResponseResults {
  private final ClientHttpResponse theResponse;

  private final String body;

  ResponseResults(final ClientHttpResponse response) throws IOException {
    this.theResponse = response;
    final InputStream bodyInputStream = response.getBody();
    this.body = IOUtils.toString(bodyInputStream, StandardCharsets.UTF_8.name());
  }

  ClientHttpResponse getTheResponse() {
    return theResponse;
  }

  public ClientHttpResponse getClientHttpResponse() {
    return this.theResponse;
  }

  public String getBody() {
    return body;
  }

  public List<String> getHeader(String headerName) {
    return theResponse.getHeaders().get(headerName);
  }
}
