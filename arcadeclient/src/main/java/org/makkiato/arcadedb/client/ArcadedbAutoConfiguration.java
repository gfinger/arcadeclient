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
    public WebClientFactory webClientFactory(ArcadedbErrorResponseFilter arcadedbErrorResponseFilter,
            WebClientSupplierStrategy webClientSupplierStrategy) {
        return new WebClientFactory(arcadedbErrorResponseFilter, webClientSupplierStrategy);
    }

    @Bean
    @ConditionalOnMissingBean
    public ArcadedbFactory arcadedbFactory(ArcadedbProperties properties, WebClientFactory webClientFactory) {
        return new ArcadedbFactory(webClientFactory, properties.getConnectionPropertiesFor(null));
    }

    @Bean
    @ConditionalOnMissingBean
    public ArcadedbConnection arcadedbConnection(ArcadedbProperties properties, WebClientFactory webClientFactory) {
        var connectionProperties = properties.getConnectionPropertiesFor(null);
        return new ArcadedbConnection(connectionProperties.getDatabase(),
                webClientFactory.getWebClientSupplierFor(connectionProperties).get());
    }
}
