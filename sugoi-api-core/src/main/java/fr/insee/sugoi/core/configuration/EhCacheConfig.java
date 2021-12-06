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

import java.util.HashMap;
import java.util.Map;
import javax.cache.Caching;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class EhCacheConfig extends CachingConfigurerSupport {

  @Bean
  @Override
  public CacheManager cacheManager() {

    org.ehcache.config.CacheConfiguration<Object, Object> cacheConfiguration =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Object.class, Object.class, ResourcePoolsBuilder.heap(1000))
            .build();
    Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();
    caches.put("Realm", cacheConfiguration);
    caches.put("Realms", cacheConfiguration);

    EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching.getCachingProvider();
    DefaultConfiguration configuration =
        new DefaultConfiguration(caches, this.getClass().getClassLoader());
    return new JCacheCacheManager(
        provider.getCacheManager(provider.getDefaultURI(), configuration));
  }
}
