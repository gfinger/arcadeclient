package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.web.request.CommandExchange;
import org.makkiato.arcadeclient.data.web.request.QueryExchange;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public interface GenericOperations {
    String getDatabaseName();
    WebClient getWebClient();

    default Mono<Boolean> script(Resource resource) throws IOException {
        return script(resource, null);
    }

    default Mono<Boolean> script(Resource resource, Map<String, Object> params)
            throws IOException {
        var commands = Arrays.stream(resource.getContentAsString(Charset.defaultCharset()).split(";"))
                .map(String::trim)
                .toArray(String[]::new);
        return script(commands, params);
    }

    default Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params)
            throws IOException {
        var command = resource.getContentAsString(Charset.defaultCharset());
        return new CommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .hasElement();
    }

    default Mono<Boolean> script(String[] commands) {
        return script(commands, null);
    }

    default Mono<Boolean> script(String[] commands, Map<String, Object> params) {
        var command = Arrays.stream(commands).map(c -> String.format("%s", c)).collect(Collectors.joining(";"));
        return new CommandExchange(CommandLanguage.SQLSCRIPT, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .hasElement();
    }

    default Flux<Map<String, Object>> command(String command) {
        return command(CommandLanguage.SQL, command, null);
    }

    default Flux<Map<String, Object>> command(String command, Map<String, Object> params) {
        return command(CommandLanguage.SQL, command, params);
    }

    default Flux<Map<String, Object>> command(CommandLanguage language, String command, Map<String, Object> params) {
        return new CommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    default Flux<Map<String, Object>> query(String query) {
        return new QueryExchange(CommandLanguage.SQL, query, getDatabaseName(), getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    enum CommandLanguage {
        SQL("sql"),
        SQLSCRIPT("sqlscript"),
        GRAPHQL("graphql"),
        CYPHER("cypher"),
        GREMLIN("gremlin"),
        MONGO("mongo");

        public final String key;
        CommandLanguage(String key) {
            this.key = key;
        }
    }
}
