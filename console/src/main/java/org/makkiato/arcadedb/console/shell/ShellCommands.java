package org.makkiato.arcadedb.console.shell;

import org.makkiato.arcadedb.client.ArcadedbClient;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.Availability;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@ShellComponent
public class ShellCommands extends AbstractShellComponent {
    private final ArcadedbClient arcadedbClient;
    private final ApplicationEventPublisher publisher;
    private ArcadedbConnection connection = null;

    public ShellCommands(ArcadedbClient arcadedbClient, ApplicationEventPublisher publisher) {
        this.arcadedbClient = arcadedbClient;
        this.publisher = publisher;
    }

    @ShellMethod(value = "Does a database exist in ArcadeDB", group = "Server")
    public Boolean exists(String name) throws ArcadeClientConfigurationException {
        return arcadedbClient.exists(name);
    }

    @ShellMethodAvailability("connectionUnavailabilityCheck")
    @ShellMethod(value = "Create a database in ArcadeDB", group = "Server")
    public String create(String name) {
        var connection = arcadedbClient.create(name);
        return connection.toString();
    }

    @ShellMethodAvailability("connectionUnavailabilityCheck")
    @ShellMethod(value = "Open a database in ArcadeDB", group = "Server")
    public String open(String name) throws ArcadeClientConfigurationException {
        connection = arcadedbClient.open(name).orElseThrow();
        publisher.publishEvent(new ConnectionUpdateEvent(this, connection));
        return String.format("opened %s", connection.name());
    }

    @ShellMethodAvailability("connectionAvailabilityCheck")
    @ShellMethod(value = "Close a database in ArcadeDB", group = "Server")
    public String close() {
        if (connection.close()) {
            var name = connection.name();
            connection = null;
            publisher.publishEvent(new ConnectionUpdateEvent(this, null));
            return String.format("closed %s", name);
        } else {
            return String.format("error when trying to close %s", connection.name());
        }
    }

    @ShellMethodAvailability("connectionAvailabilityCheck")
    @ShellMethod(value = "Send a command to ArcadeDB", group = "Database")
    public String command() throws IOException {
        StringInput component = new StringInput(getTerminal(), "Enter Value", "value");
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        StringInput.StringInputContext context = component.run(StringInput.StringInputContext.empty());
        return Arrays.stream(connection.command(context.getResultValue())).map(Object::toString).collect(Collectors.joining(", "));
    }

    public Availability connectionAvailabilityCheck() {
        return connection != null ? Availability.available() : Availability.unavailable("you are not connected to any db");
    }

    public Availability connectionUnavailabilityCheck() {
        return connection == null ? Availability.available() : Availability.unavailable("you are already connected to a db");
    }
}
