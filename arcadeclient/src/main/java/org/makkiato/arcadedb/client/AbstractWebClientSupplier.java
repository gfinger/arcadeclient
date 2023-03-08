package org.makkiato.arcadedb.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractWebClientSupplier implements WebClientSupplier {
    private WebClient[] serverSpecs = null;

    @Autowired
    private ArcadedbClient arcadedbClient;

    public WebClient getForConnectionName(String connectionName) {
        if (serverSpecs == null) {
            var connectionProperties = arcadedbClient.getConnectionPropertiesFor(connectionName);
        }
        return null;
    }
}
