package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.makkiato.arcadeclient.data.core.WebClientFactory;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = ArcadedbProperties.class)
public class TestConfiguration {
    @Bean
    public ArcadedbErrorResponseFilter arcadedbErrorResponseFilter() {
        return new ArcadedbErrorResponseFilterImpl();
    }

    @Bean
    public WebClientSupplierStrategy webClientSupplierStrategy() {
        return new HALeaderWebClientSupplierStrategy();
    }

    @Bean
    public WebClientFactory webClientFactory(ArcadedbErrorResponseFilter arcadedbErrorResponseFilter,
                                             WebClientSupplierStrategy webClientSupplierStrategy) {
        return new WebClientFactory(arcadedbErrorResponseFilter, webClientSupplierStrategy);
    }

    @Bean
    public ArcadedbFactory arcadedbFactory(ArcadedbProperties properties, WebClientFactory webClientFactory) {
        return new ArcadedbFactory(webClientFactory, properties.getConnectionPropertiesFor(null));
    }

    @Bean
    public ArcadedbTemplate arcadedbTemplate(ArcadedbProperties properties, WebClientFactory webClientFactory) {
        var connectionProperties = properties.getConnectionPropertiesFor(null);
        return new ArcadedbTemplate(webClientFactory.getWebClientSupplierFor(connectionProperties).get(), connectionProperties.getDatabase()
        );
    }
}
