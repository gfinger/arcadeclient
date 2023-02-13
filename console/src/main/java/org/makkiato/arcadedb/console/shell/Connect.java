package org.makkiato.arcadedb.console.shell;

import org.makkiato.arcadedb.client.ArcadedbClient;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Arrays;
import java.util.stream.Collectors;

@ShellComponent
public class Connect {
    private final ArcadedbClient arcadedbClient;
    private final ApplicationEventPublisher publisher;
    private ArcadedbConnection connection = null;

    public Connect(ArcadedbClient arcadedbClient, ApplicationEventPublisher publisher) {
        this.arcadedbClient = arcadedbClient;
        this.publisher = publisher;
    }

    @ShellMethod("Does a database exist in ArcadeDB")
    public Boolean exists(String name) throws ArcadeClientConfigurationException {
        return arcadedbClient.exists(name);
    }

    @ShellMethod("Create a database in ArcadeDB")
    public String create(String name) {
        var connection = arcadedbClient.create(name);
        return connection.toString();
    }

    @ShellMethod("Open a database in ArcadeDB")
    public String open(String name) throws ArcadeClientConfigurationException {
        connection = arcadedbClient.open(name).orElseThrow();
        publisher.publishEvent(new ConnectionUpdateEvent(this, connection));
        return String.format("opened %s", connection.name());
    }

    @ShellMethod("Close a database in ArcadeDB")
    public String close() {
        if (connection == null) {
            return "no connection open";
        }
        if (connection.close()) {
            var name = connection.name();
            connection = null;
            publisher.publishEvent(new ConnectionUpdateEvent(this, null));
            return String.format("closed %s", name);
        } else {
            return String.format("error when trying to close %s", connection.name());
        }
    }

    @ShellMethod("Send a command to ArcadeDB")
    public String command(String command) {
        if(connection != null) {
            return Arrays.stream(connection.command(command)).map(Object::toString).collect(Collectors.joining(", "));
        }
        else {
            return "no connection open";
        }
    }
}
