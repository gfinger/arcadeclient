package org.makkiato.arcadedb.console.shell;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

public class DatabaseUpdateEvent extends ApplicationEvent {
    @Getter
    private final String databaseName;

    public DatabaseUpdateEvent(Object source, String databaseName) {
        super(source);
        this.databaseName = databaseName;
    }
}
