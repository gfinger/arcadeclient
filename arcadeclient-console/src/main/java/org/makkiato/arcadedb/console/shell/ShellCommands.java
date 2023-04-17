package org.makkiato.arcadedb.console.shell;

import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.makkiato.arcadeclient.data.core.WebClientFactory;
import org.makkiato.arcadeclient.data.exception.client.ArcadeClientConfigurationException;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.shell.Availability;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.Itemable;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;

@ShellComponent
public class ShellCommands extends AbstractShellComponent {
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private final ApplicationEventPublisher publisher;
    private ArcadedbFactory arcadedbFactory;
    private ArcadedbTemplate connection = null;
    private ArcadedbProperties arcadedbProperties;
    private WebClientFactory arcadedbClient;

    public ShellCommands(ArcadedbProperties arcadedbProperties, ArcadedbFactory arcadedbFactory, WebClientFactory arcadedbClient,
            ApplicationEventPublisher publisher) {
        this.arcadedbProperties = arcadedbProperties;
        this.arcadedbFactory = arcadedbFactory;
        this.arcadedbClient = arcadedbClient;
        this.publisher = publisher;
    }

    @ShellMethodAvailability("connectionClosedCheck")
    @ShellMethod(value = "Select the server to work with", group = "Server")
    public String selectServer() {
        var connectionProperties = arcadedbProperties.getConnectionPropertiesMap();
        var items = connectionProperties.keySet().stream().map(key -> SelectorItem.of(key, key)).toList();
        var component = new SingleItemSelector<>(getTerminal(), items, "choose configured connection from list", null);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        var context = component.run(SingleItemSelector.SingleItemSelectorContext.empty());
        context.getResultItem().map(Itemable::getItem).ifPresent(serverName -> {
            try {
                setArcadedbFactory(new ArcadedbFactory(arcadedbClient, connectionProperties.get(serverName)),
                        serverName);
            } catch (ArcadeClientConfigurationException e) {
                setArcadedbFactory(null, null);
                throw new RuntimeException(e);
            }
        });
        return getArcadedbFactory() != null ? "OK" : "Server was not configured";
    }

    @ShellMethodAvailability("factoryAvailableCheck")
    @ShellMethod(value = "Does a database exist in ArcadeDB", group = "Server")
    public Boolean exists() throws ArcadeClientConfigurationException {
        return this.getArcadedbFactory().exists().block(CONNECTION_TIMEOUT);
    }

    @ShellMethodAvailability("factoryAvailableCheck")
    @ShellMethod(value = "Create a database in ArcadeDB", group = "Server")
    public String create() {
        var ok = getArcadedbFactory().create().block(CONNECTION_TIMEOUT);
        return ok ? "db created" : "error creating db";
    }

    @ShellMethodAvailability("connectionClosedAndFactoryAvailableCheck")
    @ShellMethod(value = "Open a database in ArcadeDB", group = "Server")
    public String open() {
        var ok = getArcadedbFactory().open().block(CONNECTION_TIMEOUT);
        return ok? "db opened":"error opening db";
    }

    @ShellMethodAvailability("connectionAvailableCheck")
    @ShellMethod(value = "Close a database in ArcadeDB", group = "Server")
    public String close() {
        getArcadedbFactory().close();
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
        return getConnection().command(context.getResultValue()).map(Object::toString).collect(Collectors.joining(", "))
                .block(CONNECTION_TIMEOUT);
    }

    @ShellMethodAvailability("connectionAvailableCheck")
    @ShellMethod(value = "Send the content of a sqlscript file to ArcadeDB", group = "Database")
    public Boolean load(String path) throws IOException {
        var file = ResourceUtils.getFile(path);
        var script = new FileSystemResource(file);
        return getConnection().script(script).block(CONNECTION_TIMEOUT);
    }

    public Availability connectionAvailableCheck() {
        return getConnection() != null ? Availability.available()
                : Availability.unavailable("you have to close the current connection first");
    }

    public Availability connectionClosedAndFactoryAvailableCheck() {
        return getArcadedbFactory() != null && getConnection() == null ? Availability.available()
                : Availability.unavailable("you have to close the current connection first");
    }

    public Availability connectionClosedAndFactoryNotAvailableCheck() {
        return getArcadedbFactory() == null && getConnection() == null ? Availability.available()
                : Availability.unavailable("you have to close the current connection first");
    }

    public Availability connectionClosedCheck() {
        return getConnection() == null ? Availability.available()
                : Availability.unavailable("you have to close the current connection first");
    }

    public Availability factoryAvailableCheck() {
        return getArcadedbFactory() != null ? Availability.available()
                : Availability.unavailable("you have to select a server first");
    }

    private ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    private ArcadedbTemplate getConnection() {
        return connection;
    }

    private void setConnection(ArcadedbTemplate connection) {
        this.connection = connection;
        getPublisher()
                .publishEvent(new DatabaseUpdateEvent(this, null));
    }

    public ArcadedbFactory getArcadedbFactory() {
        return arcadedbFactory;
    }

    public void setArcadedbFactory(ArcadedbFactory arcadedbFactory, String configurationName) {
        this.arcadedbFactory = arcadedbFactory;
        getPublisher().publishEvent(new ServerUpdateEvent(this, configurationName));
    }
}
