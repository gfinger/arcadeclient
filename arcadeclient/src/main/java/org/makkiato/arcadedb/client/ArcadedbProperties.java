package org.makkiato.arcadedb.client;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix="org.makkiato.arcadedb")
@Data
public class ArcadedbProperties {
    @Data
    public static class ConnectionProperties {
        private Integer port = Integer.valueOf(DEFAULT_PORT);
        private String host = DEFAULT_HOST;
        private Boolean leaderPreferred = false;
        private String username = DEFAULT_USERNAME;
        private String password = "";
    }

    public static final int DEFAULT_PORT = 2480;
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_USERNAME = "root";

    private Map<String, ConnectionProperties> connections;
    private Boolean enabled = false;
}
