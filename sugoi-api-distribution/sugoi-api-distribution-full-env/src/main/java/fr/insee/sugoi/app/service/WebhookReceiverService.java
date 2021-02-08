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
package fr.insee.sugoi.app.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import fr.insee.sugoi.app.service.utils.QueuedDaemonThreadPool;
import wiremock.org.eclipse.jetty.util.thread.ThreadPool;

public class WebhookReceiverService {

  public static boolean startServer() {
    WireMockServer webhookServer =
        new WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(8089)
                .notifier(new Slf4jNotifier(true))
                .threadPoolFactory(
                    new ThreadPoolFactory() {

                      @Override
                      public ThreadPool buildThreadPool(Options options) {
                        ThreadPool tp = new QueuedDaemonThreadPool(options.containerThreads());
                        return tp;
                      }
                    }));
    webhookServer.start();

    webhookServer.stubFor(post("/spoc/send").willReturn(aResponse().withStatus(200)));
    return true;
  }
}
