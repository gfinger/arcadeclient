package org.makkiato.arcadedb.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "org.makkiato.arcadedb")
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ArcadedbProperties {
    public static final int DEFAULT_PORT = 2480;
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_USERNAME = "root";
    public static final Integer MAX_SERVER_INFO_RETRIES = 3;
    public static final Integer CONNECTION_TIMEOUT_SECS = 3;
    /**
     * A map of configuration properties required to connect to an ArcadeDB server or cluster.
     * The name of configuration can be chosen freely.
     * It is used when creating an ArcadedbFactory by an ArcadedbClient.
     */
    private Map<String, ConnectionProperties> connections;
    /**
     * If true, enables the autoconfiguration.
     */
    private Boolean enabled = false;

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
         * Should the leader in a high availability cluster be preferred when choosing the server to connect to.
         */
        private Boolean leaderPreferred = false;
        /**
         * Username to be used for connection.
         */
        private String username = DEFAULT_USERNAME;
        /**
         * Password of user.
         */
        private String password = "";
        private Integer maxServerInfoRetries = MAX_SERVER_INFO_RETRIES;
        private Integer connectionTimeoutSecs = CONNECTION_TIMEOUT_SECS;
    }
}
