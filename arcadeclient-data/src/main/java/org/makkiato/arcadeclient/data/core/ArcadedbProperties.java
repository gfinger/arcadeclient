package org.makkiato.arcadeclient.data.core;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.makkiato.arcadeclient.data.exception.client.ArcadeClientConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ConfigurationProperties(prefix = "org.makkiato.arcadedb")
@NoArgsConstructor
@AllArgsConstructor
public class ArcadedbProperties {
    public static final int DEFAULT_PORT = 2480;
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_CONFIGURATION_NAME = "default";
    public static final Integer MAX_SERVER_INFO_RETRIES = 3;
    public static final Integer CONNECTION_TIMEOUT_SECS = 3;
    /**
     * A map of configuration properties required to connect to an ArcadeDB server
     * or cluster.
     * The name of configuration can be chosen freely.
     * It is used when creating an ArcadedbFactory by an ArcadedbClient.
     */
    @Setter
    private Map<String, ConnectionProperties> connections;

    /**
     * A single configuration to be used as alternative for a map of configurations
     * with one entry only.
     * This configuration is used by default, even in the presence of a
     * configuration map.
     */
    @Setter
    private ConnectionProperties connection;

    @Setter
    private String defaultConfigurationName = DEFAULT_CONFIGURATION_NAME;

    public ConnectionProperties getConnectionPropertiesFor(String connectionName) {
        ConnectionProperties cp = null;
        var connectionPropertiesMap = getConnectionPropertiesMap();
        if (connectionName != null) {
            cp = connectionPropertiesMap.get(connectionName);
        } else if (connectionPropertiesMap.containsKey(defaultConfigurationName)) {
            cp = connectionPropertiesMap.get(defaultConfigurationName);
        } else if (connectionPropertiesMap.size() > 0) {
            cp = connectionPropertiesMap.entrySet().stream().findFirst().get().getValue();
        }
        if (cp == null) {
            throw new ArcadeClientConfigurationException(
                    String.format("Missing configuration for database: %s",
                            connectionName));
        }
        return cp;
    }

    public Map<String, ConnectionProperties> getConnectionPropertiesMap() {
        var connectionProperties = connections != null ? connections : new HashMap<String, ConnectionProperties>();
        if (connection != null) {
            connectionProperties.put(defaultConfigurationName, connection);
        }
        return connectionProperties;
    }

    @Data
    public static class ConnectionProperties {
        /**
         * Port used for connection.
         */
        private Integer port = DEFAULT_PORT;
        /**
         * Hostname or IP-Address for connection.
         */
        private String host = DEFAULT_HOST;
        /**
         * Should the leader in a high availability cluster be preferred when choosing
         * the server to connect to.
         */
        private Boolean leaderPreferred = false;
        /**
         * Username to be used for connection.
         */
        @NotBlank
        private String username;
        /**
         * Password of user.
         */
        @NotBlank
        private String password;
        /**
         * Name of default database
         */
        @NotBlank
        private String database;
        private Integer maxServerInfoRetries = MAX_SERVER_INFO_RETRIES;
        private Integer connectionTimeoutSecs = CONNECTION_TIMEOUT_SECS;

        public Duration getConnectionTimeout() {
            return Duration.ofSeconds(getConnectionTimeoutSecs());
        }
    }
}
