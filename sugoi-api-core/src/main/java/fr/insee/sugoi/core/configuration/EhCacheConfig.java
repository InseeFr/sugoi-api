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

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.ehcache.config.CacheConfiguration;

@Configuration
@EnableCaching
public class EhCacheConfig implements CachingConfigurer {

        @Bean(destroyMethod = "shutdown")
        public net.sf.ehcache.CacheManager ehCacheManager() {

                CacheConfiguration cacheConfiguration = new CacheConfiguration();
                cacheConfiguration.setName("Realms");
                cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
                cacheConfiguration.setMaxEntriesLocalHeap(1000);

                CacheConfiguration cacheConfiguration1 = new CacheConfiguration();
                cacheConfiguration1.setName("Realm");
                cacheConfiguration1.setMemoryStoreEvictionPolicy("LRU");
                cacheConfiguration1.setMaxEntriesLocalHeap(1000);

                net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();

                config.addCache(cacheConfiguration);
                config.addCache(cacheConfiguration1);

                return net.sf.ehcache.CacheManager.newInstance(config);
        }

        @Bean
        @Override
        public CacheManager cacheManager() {
                return new EhCacheCacheManager(ehCacheManager());
        }

        @Bean
        @Override
        public KeyGenerator keyGenerator() {
                return new SimpleKeyGenerator();
        }

        @Bean
        @Override
        public CacheResolver cacheResolver() {
                return new SimpleCacheResolver(cacheManager());
        }

        @Bean
        @Override
        public CacheErrorHandler errorHandler() {
                return new SimpleCacheErrorHandler();
        }
}