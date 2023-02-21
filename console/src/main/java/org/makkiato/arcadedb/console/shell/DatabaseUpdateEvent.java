package org.makkiato.arcadedb.console.shell;

import lombok.Getter;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.springframework.context.ApplicationEvent;


public class DatabaseUpdateEvent extends ApplicationEvent {
    @Getter
    private final String databaseName;

    public DatabaseUpdateEvent(Object source,String databaseName) {
        super(source);
        this.databaseName = databaseName;
    }
}
