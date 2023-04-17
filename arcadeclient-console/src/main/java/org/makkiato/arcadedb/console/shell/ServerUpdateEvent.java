package org.makkiato.arcadedb.console.shell;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public final class ServerUpdateEvent extends ApplicationEvent {
    @Getter
    private final String serverName;

    public ServerUpdateEvent(Object sender, String serverName) {
        super(sender);
        this.serverName = serverName;
    }
}

