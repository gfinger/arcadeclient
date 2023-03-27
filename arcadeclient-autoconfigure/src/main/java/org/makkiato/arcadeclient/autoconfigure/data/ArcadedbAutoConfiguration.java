package org.makkiato.arcadeclient.autoconfigure.data;

import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.makkiato.arcadeclient.data.core.WebClientFactory;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ArcadedbProperties.class)
@ConditionalOnClass(ArcadedbProperties.class)
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
    public ArcadedbTemplate arcadedbTemplate(ArcadedbProperties properties, WebClientFactory webClientFactory) {
        var connectionProperties = properties.getConnectionPropertiesFor(null);
        return new ArcadedbTemplate(webClientFactory.getWebClientSupplierFor(connectionProperties).get(), connectionProperties.getDatabase()
        );
    }
}
