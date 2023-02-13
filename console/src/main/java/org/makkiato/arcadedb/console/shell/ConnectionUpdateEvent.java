package org.makkiato.arcadedb.console.shell;

import lombok.Getter;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.springframework.context.ApplicationEvent;


public class ConnectionUpdateEvent extends ApplicationEvent {
    @Getter
    private final ArcadedbConnection connection;

    public ConnectionUpdateEvent(Object source, ArcadedbConnection connection) {
        super(source);
        this.connection = connection;
    }
}
