package org.makkiato.arcadedb.client;

import org.makkiato.arcadedb.client.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadedb.client.web.ArcadedbErrorResponseFilterImpl;
import org.makkiato.arcadedb.client.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadedb.client.web.client.WebClientSupplierStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ArcadedbProperties.class)
public class ArcadedbAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ArcadedbErrorResponseFilter arcadedbErrorResponseFilter() {
        return new ArcadedbErrorResponseFilterImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientSupplierStrategy webClientSupplierStrategy() {
        return new HALeaderWebClientSupplierStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public ArcadedbClient arcadedbClient(ArcadedbErrorResponseFilter arcadedbErrorResponseFilter,
            WebClientSupplierStrategy webClientSupplierStrategy) {
        return new ArcadedbClient(arcadedbErrorResponseFilter, webClientSupplierStrategy);
    }

    @Bean
    @ConditionalOnMissingBean
    public ArcadedbFactory arcadedbFactory(ArcadedbProperties properties, ArcadedbClient arcadedbClient) {
        return new ArcadedbFactory(arcadedbClient, properties.getConnectionPropertiesFor(null));
    }
}
