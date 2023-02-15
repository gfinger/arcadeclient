package org.makkiato.arcadedb.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.http.client.reactive.JdkHttpClientResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

@AutoConfiguration
@ConditionalOnProperty(prefix = "org.makkiato.arcadedb", name = "enabled")
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
