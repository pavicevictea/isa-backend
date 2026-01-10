package com.isa.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public org.springframework.cache.CacheManager cacheManager() throws Exception {
        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = provider.getCacheManager(
                getClass().getResource("/ehcache.xml").toURI(),
                getClass().getClassLoader()
        );
        return new JCacheCacheManager(cacheManager);
    }
}
