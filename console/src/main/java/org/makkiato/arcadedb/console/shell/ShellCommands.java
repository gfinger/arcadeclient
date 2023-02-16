package org.makkiato.arcadedb.console.shell;

import org.makkiato.arcadedb.client.ArcadedbClient;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.exception.ArcadeConnectionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.Availability;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @ShellMethodAvailability("connectionClosedCheck")
    @ShellMethod(value = "Select the connection to work with", group = "Server")
    public String openConnection() throws ArcadeClientConfigurationException {
        List<SelectorItem<String>> items = getArcadedbClient().getConnectionPropertiesMap().keySet().stream().map(key -> SelectorItem.of(key, key)).toList();
        SingleItemSelector<String, SelectorItem<String>> component = new SingleItemSelector<>(getTerminal(), items, "choose configured connection from list", null);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = component.run(SingleItemSelector.SingleItemSelectorContext.empty());
        String connectionName = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).get();
        this.setConnection(getArcadedbClient().createConnectionFor(connectionName));
        getPublisher().publishEvent(new ConnectionUpdateEvent(this, getConnection()));
        return this.getConnection() != null ? "OK" : "Connection was not created";
    }

    @ShellMethodAvailability("databaseClosedCheck")
    @ShellMethod(value = "Close the current connection", group = "Server")
    public String closeConnection() {
        getPublisher().publishEvent(new ConnectionUpdateEvent(this, getConnection()));
        this.setConnection(null);
        return "OK";
    }

    @ShellMethodAvailability("connectionAvailabilityCheck")
    @ShellMethod(value = "Does a database exist in ArcadeDB", group = "Server")
    public Boolean exists(String dbName) throws ArcadeClientConfigurationException {
        return this.getConnection().exists(dbName);
    }

    @ShellMethodAvailability("databaseClosedCheck")
    @ShellMethod(value = "Create a database in ArcadeDB", group = "Server")
    public String create(String dbName) {
        if (this.getConnection().create(dbName)) {
            this.getConnection().setDatabaseName(dbName);
            getPublisher().publishEvent(new ConnectionUpdateEvent(this, getConnection()));
        }
        return String.format("created %s", dbName);
    }

    @ShellMethodAvailability("databaseClosedCheck")
    @ShellMethod(value = "Open a database in ArcadeDB", group = "Server")
    public String open(String dbName) throws ArcadeConnectionException {
        if (this.getConnection().open(dbName)) {
            this.getConnection().setDatabaseName(dbName);
            getPublisher().publishEvent(new ConnectionUpdateEvent(this, getConnection()));
        }
        return String.format("opened %s", dbName);
    }

    @ShellMethodAvailability("databaseAvailabilityCheck")
    @ShellMethod(value = "Close a database in ArcadeDB", group = "Server")
    public String close() {
        var dbName = this.getConnection().getDatabaseName();
        if (this.getConnection().close()) {
            getPublisher().publishEvent(new ConnectionUpdateEvent(this, null));
            return String.format("closed %s", dbName);
        } else {
            return String.format("error when trying to close %s", dbName);
        }
    }

    @ShellMethodAvailability("databaseAvailabilityCheck")
    @ShellMethod(value = "Send a command to ArcadeDB", group = "Database")
    public String command() throws ArcadeConnectionException {
        StringInput component = new StringInput(getTerminal(), "Enter Value", "value");
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        StringInput.StringInputContext context = component.run(StringInput.StringInputContext.empty());
        return Arrays.stream(getConnection().command(context.getResultValue())).map(Object::toString).collect(Collectors.joining(", "));
    }

    public Availability connectionAvailabilityCheck() {
        return this.getConnection() != null ? Availability.available() : Availability.unavailable("you have not opened a connection");
    }

    public Availability connectionClosedCheck() {
        return this.getConnection() == null ? Availability.available() : Availability.unavailable("you have to close the current connection first");
    }

    public Availability databaseAvailabilityCheck() {
        return this.getConnection() != null && this.getConnection().getDatabaseName() != null ? Availability.available() : Availability.unavailable("you have not opened a database");
    }

    public Availability databaseClosedCheck() {
        return this.getConnection() != null && this.getConnection().getDatabaseName() == null ? Availability.available() : Availability.unavailable("you have close the current database first");
    }

    public ArcadedbClient getArcadedbClient() {
        return arcadedbClient;
    }

    public ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    public ArcadedbConnection getConnection() {
        return connection;
    }

    public void setConnection(ArcadedbConnection connection) {
        this.connection = connection;
        getPublisher().publishEvent(new ConnectionUpdateEvent(this, getConnection()));
    }
}
