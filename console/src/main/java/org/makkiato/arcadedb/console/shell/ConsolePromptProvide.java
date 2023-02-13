package org.makkiato.arcadedb.console.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.springframework.context.event.EventListener;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ConsolePromptProvide implements PromptProvider {
    ArcadedbConnection connection;
    @Override
    public AttributedString getPrompt() {
        if(connection != null) {
            return new AttributedString(String.format("%s >", connection.name()),AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        } else {
            return new AttributedString(">", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        }
    }

    @EventListener
    public void handle(ConnectionUpdateEvent event) {
        this.connection = event.getConnection();
    }
}
