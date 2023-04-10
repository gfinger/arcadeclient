package org.makkiato.arcadeclient.data.operations;

import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

public interface GenericOperations {
    String getDatabaseName();

    WebClient getWebClient();

    Mono<Boolean> script(Resource resource) throws IOException;

    Mono<Boolean> script(Resource resource, Map<String, Object> params)
            throws IOException;

    Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params)
            throws IOException;

    Mono<Boolean> script(String[] commands);

    Mono<Boolean> script(String[] commands, Map<String, Object> params);

    Flux<Map<String, Object>> command(String command);

    Flux<Map<String, Object>> command(String command, Map<String, Object> params);

    Flux<Map<String, Object>> command(CommandLanguage language, String command, Map<String, Object> params);

    Flux<Map<String, Object>> query(String query);

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
