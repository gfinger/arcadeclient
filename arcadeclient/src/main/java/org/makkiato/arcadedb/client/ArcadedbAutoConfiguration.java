package org.makkiato.arcadedb.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "org.makkiato.arcadedb", name = "enabled")
@EnableConfigurationProperties(ArcadedbProperties.class)
public class ArcadedbAutoConfiguration {
    private final ArcadedbProperties properties;

    public ArcadedbAutoConfiguration(ArcadedbProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ArcadedbClient arcadedbClient() {
        return new ArcadedbClient(properties.getConnections());
    }

}
