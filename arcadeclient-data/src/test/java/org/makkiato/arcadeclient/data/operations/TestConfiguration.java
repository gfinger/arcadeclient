package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.config.ArcadeclientConfigurationSupport;
import org.makkiato.arcadeclient.data.core.ConnectionProperties;
import org.makkiato.arcadeclient.data.core.WebClientSupplierFactory;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = ConnectionProperties.class)
public class TestConfiguration extends ArcadeclientConfigurationSupport {
    @Bean
    public ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter() {
        return new ArcadeclientErrorResponseFilterImpl();
    }

    @Bean
    public WebClientSupplierStrategy webClientSupplierStrategy() {
        return new HALeaderWebClientSupplierStrategy();
    }

    @Bean
    public WebClientSupplierFactory webClientFactory(ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter,
                                             WebClientSupplierStrategy webClientSupplierStrategy) {
        return new WebClientSupplierFactory(arcadedbErrorResponseFilter, webClientSupplierStrategy);
    }
}
