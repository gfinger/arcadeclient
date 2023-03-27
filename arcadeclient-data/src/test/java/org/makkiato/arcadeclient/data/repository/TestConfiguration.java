package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.config.ArcadeclientConfigurationSupport;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.makkiato.arcadeclient.data.core.WebClientFactory;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = ArcadedbProperties.class)
public class TestConfiguration extends ArcadeclientConfigurationSupport {
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
}
