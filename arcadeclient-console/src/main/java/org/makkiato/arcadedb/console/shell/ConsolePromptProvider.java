package org.makkiato.arcadedb.console.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.event.EventListener;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ConsolePromptProvider implements PromptProvider {
    String databaseName;
    String serverName;

    @Override
    public AttributedString getPrompt() {
        if (serverName != null) {
            var prompt = new AttributedString(String.format("%s> ", serverName),
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
            if (databaseName != null) {
                prompt = AttributedString.join(AttributedString.fromAnsi(""),
                        prompt, new AttributedString(String.format("%s> ", databaseName),
                                AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)));
            }
            return prompt;
        } else {
            if (databaseName != null) {
                return new AttributedString(String.format("%s> ", databaseName),
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA));
            } else {
                return new AttributedString(">", AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA));
            }
        }
    }

    @EventListener
    public void handle(DatabaseUpdateEvent event) {
        this.databaseName = event.getDatabaseName();
    }

    @EventListener
    public void handle(ServerUpdateEvent event) {
        this.serverName = event.getServerName();
    }
}
