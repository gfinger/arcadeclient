package org.makkiato.arcadedb.client;

import org.makkiato.arcadedb.client.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadedb.client.web.ArcadedbErrorResponseFilterImpl;
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
    public ArcadedbClient arcadedbClient(ArcadedbErrorResponseFilter arcadedbErrorResponseFilter) {
        return new ArcadedbClient(arcadedbErrorResponseFilter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ArcadedbFactory arcadedbFactory(ArcadedbProperties properties, ArcadedbClient arcadedbClient) {
        return new ArcadedbFactory(arcadedbClient, properties.getConnectionPropertiesFor(null));
    }
}
