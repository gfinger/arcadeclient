package org.makkiato.arcadedb.console.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.springframework.context.event.EventListener;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ConsolePromptProvider implements PromptProvider {
    ArcadedbConnection connection;
    @Override
    public AttributedString getPrompt() {
        if(connection != null && connection.getConnectionName() != null) {
            var prompt = new AttributedString(String.format("%s>", connection.getConnectionName()), AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
            if(connection.getDatabaseName() != null) {
                prompt = AttributedString.join(AttributedString.fromAnsi(""), prompt, new AttributedString(String.format("%s>", connection.getDatabaseName()), AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)));
            }
            return prompt;
        } else {
            return new AttributedString(">", AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA));
        }
    }

    @EventListener
    public void handle(ConnectionUpdateEvent event) {
        this.connection = event.getConnection();
    }
}
