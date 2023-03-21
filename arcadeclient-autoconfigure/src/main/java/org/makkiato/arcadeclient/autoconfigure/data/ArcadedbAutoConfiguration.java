package org.makkiato.arcadeclient.autoconfigure.data;

import org.makkiato.arcadeclient.data.core.*;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(ArcadedbProperties.class)
@ConditionalOnClass(ArcadedbProperties.class)
@Import(ArcadeclientRepositoriesRegistrar.class)
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
    public ArcadedbTemplate arcadedbConnection(ArcadedbProperties properties, WebClientFactory webClientFactory) {
        var connectionProperties = properties.getConnectionPropertiesFor(null);
        return new ArcadedbTemplate(connectionProperties.getDatabase(),
                webClientFactory.getWebClientSupplierFor(connectionProperties).get());
    }
}
