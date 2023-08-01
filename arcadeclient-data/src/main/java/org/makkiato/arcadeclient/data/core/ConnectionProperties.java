package org.makkiato.arcadeclient.data.core;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@ConfigurationProperties(prefix = "org.makkiato.arcadedb")
@Data
public class ConnectionProperties {
    public static final int DEFAULT_PORT = 2480;
    public static final String DEFAULT_HOST = "localhost";
    public static final Integer MAX_SERVER_INFO_RETRIES = 3;
    public static final Integer CONNECTION_TIMEOUT_SECS = 3;
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
     * Name of defualt database
     */
    @NotBlank
    private String database;

    private Integer maxServerInfoRetries = MAX_SERVER_INFO_RETRIES;
    private Integer connectionTimeoutSecs = CONNECTION_TIMEOUT_SECS;

    public Duration getConnectionTimeout() {
        return Duration.ofSeconds(getConnectionTimeoutSecs());
    }
}
