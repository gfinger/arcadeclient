package org.makkiato.arcadedb.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ArcadedbProperties.class)
public class ArcadedbClientITConfiguration {
    @Bean
    public ArcadedbClient arcadedbClient(ArcadedbProperties properties) {
        return new ArcadedbClient(properties.getConnections());
    }
}
