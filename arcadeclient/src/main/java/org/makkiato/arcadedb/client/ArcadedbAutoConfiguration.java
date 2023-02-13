package org.makkiato.arcadedb.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ArcadedbClient.class)
@EnableConfigurationProperties(ArcadedbProperties.class)
public class ArcadedbAutoConfiguration {
    private final ArcadedbProperties properties;

    public ArcadedbAutoConfiguration(ArcadedbProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnMissingBean(ArcadedbClient.class)
    @Bean
    public ArcadedbClient remoteDatabase() {
        return new ArcadedbClient(properties.getConnections());
    }
}
