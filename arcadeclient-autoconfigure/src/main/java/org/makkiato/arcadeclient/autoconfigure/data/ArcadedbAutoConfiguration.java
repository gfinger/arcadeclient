package org.makkiato.arcadeclient.autoconfigure.data;

import org.makkiato.arcadeclient.data.core.ConnectionProperties;
import org.makkiato.arcadeclient.data.core.WebClientSupplierFactory;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ConnectionProperties.class)
@ConditionalOnClass(ConnectionProperties.class)
public class ArcadedbAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter() {
        return new ArcadeclientErrorResponseFilterImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientSupplierStrategy webClientSupplierStrategy() {
        return new HALeaderWebClientSupplierStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientSupplierFactory webClientFactory(ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter,
            WebClientSupplierStrategy webClientSupplierStrategy) {
        return new WebClientSupplierFactory(arcadedbErrorResponseFilter, webClientSupplierStrategy);
    }
}
