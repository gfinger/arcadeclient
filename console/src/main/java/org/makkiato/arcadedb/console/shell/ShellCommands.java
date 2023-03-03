package org.makkiato.arcadedb.console.shell;

import org.makkiato.arcadedb.client.ArcadedbClient;
import org.makkiato.arcadedb.client.ArcadedbConnection;
import org.makkiato.arcadedb.client.ArcadedbFactory;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientConfigurationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.Availability;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.Itemable;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.time.Duration;
import java.util.stream.Collectors;

@ShellComponent
public class ShellCommands extends AbstractShellComponent {
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private final ApplicationEventPublisher publisher;
    private final ArcadedbClient arcadedbClient;
    private ArcadedbFactory arcadedbFactory;
    private ArcadedbConnection connection = null;

    public ShellCommands(ArcadedbClient arcadedbClient, ApplicationEventPublisher publisher) {
        this.arcadedbClient = arcadedbClient;
        this.publisher = publisher;
    }

    @ShellMethodAvailability("connectionClosedCheck")
    @ShellMethod(value = "Select the server to work with", group = "Server")
    public String selectServer() {
        var items = getArcadedbClient().getConnectionPropertiesMap().keySet().stream().map(key -> SelectorItem.of(key, key)).toList();
        var component = new SingleItemSelector<>(getTerminal(), items, "choose configured connection from list", null);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        var context = component.run(SingleItemSelector.SingleItemSelectorContext.empty());
        context.getResultItem().map(Itemable::getItem).ifPresent(serverName -> {
            try {
                setArcadedbFactory(getArcadedbClient().createFactoryFor(serverName));
            } catch (ArcadeClientConfigurationException e) {
                setArcadedbFactory(null);
                throw new RuntimeException(e);
            }
        });
        return getArcadedbFactory() != null ? "OK" : "Server was not configured";
    }

    @ShellMethodAvailability("factoryAvailableCheck")
    @ShellMethod(value = "Does a database exist in ArcadeDB", group = "Server")
    public Boolean exists(String dbName) throws ArcadeClientConfigurationException {
        return this.getArcadedbFactory().exists(dbName).block(CONNECTION_TIMEOUT);
    }

    @ShellMethodAvailability("factoryAvailableCheck")
    @ShellMethod(value = "Create a database in ArcadeDB", group = "Server")
    public String create(String dbName) {
        setConnection(null);
        var connection = getArcadedbFactory().create(dbName).block(CONNECTION_TIMEOUT);
        setConnection(connection);
        return String.format("created %s", dbName);
    }

    @ShellMethodAvailability("connectionClosedAndFactoryAvailableCheck")
    @ShellMethod(value = "Open a database in ArcadeDB", group = "Server")
    public String open(String dbName) {
        setConnection(null);
        var connection = getArcadedbFactory().open(dbName).block(CONNECTION_TIMEOUT);
        setConnection(connection);
        return String.format("opened %s", dbName);
    }

    @ShellMethodAvailability("connectionAvailableCheck")
    @ShellMethod(value = "Close a database in ArcadeDB", group = "Server")
    public String close() {
        getConnection().close();
        setConnection(null);
        return "closed";
    }

    @ShellMethodAvailability("connectionAvailableCheck")
    @ShellMethod(value = "Send a command to ArcadeDB", group = "Database")
    public String command() {
        StringInput component = new StringInput(getTerminal(), ">> ", "");
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        StringInput.StringInputContext context = component.run(StringInput.StringInputContext.empty());
        return getConnection().command(context.getResultValue()).map(Object::toString).collect(Collectors.joining(", ")).block(CONNECTION_TIMEOUT);
    }

    public Availability connectionAvailableCheck() {
        return getConnection() != null ? Availability.available() : Availability.unavailable("you have to close the current connection first");
    }

    public Availability connectionClosedAndFactoryAvailableCheck() {
        return getArcadedbFactory() != null && getConnection() == null ? Availability.available() : Availability.unavailable("you have to close the current connection first");
    }

    public Availability connectionClosedCheck() {
        return getConnection() == null ? Availability.available() : Availability.unavailable("you have to close the current connection first");
    }

    public Availability factoryAvailableCheck() {
        return getArcadedbFactory() != null ? Availability.available() : Availability.unavailable("you have to select a server first");
    }

    private ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    private ArcadedbClient getArcadedbClient() {
        return arcadedbClient;
    }

    private ArcadedbConnection getConnection() {
        return connection;
    }

    private void setConnection(ArcadedbConnection connection) {
        this.connection = connection;
        getPublisher().publishEvent(new DatabaseUpdateEvent(this, connection != null ? connection.getDatabaseName() : null));
    }

    public ArcadedbFactory getArcadedbFactory() {
        return arcadedbFactory;
    }

    public void setArcadedbFactory(ArcadedbFactory arcadedbFactory) {
        this.arcadedbFactory = arcadedbFactory;
        getPublisher().publishEvent(new ServerUpdateEvent(this, arcadedbFactory.getName()));
    }
}
