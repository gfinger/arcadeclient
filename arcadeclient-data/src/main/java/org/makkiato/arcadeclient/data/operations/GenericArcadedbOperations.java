package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

public interface GenericArcadedbOperations {
    Mono<Boolean> script(Resource resource) throws IOException;

    Mono<Boolean> script(Resource resource, Map<String, Object> params)
            throws IOException;

    Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params) throws IOException;

    Mono<Boolean> script(String[] commands);

    Mono<Boolean> script(String[] commands, Map<String, Object> params);

    Flux<Map<String, Object>> command(String command);

    Flux<Map<String, Object>> command(String command, Map<String, Object> params);

    Flux<Map<String, Object>> command(CommandLanguage language, String command, Map<String, Object> params);

    <T extends IdentifiableDocumentBase> Flux<T> select(String command, Class<T> objectType);

    <T extends IdentifiableDocumentBase> Flux<T> select(String command, Map<String, Object> params, Class<T> objectType);

    <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Class<T> objectType);

    <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String, Object> params,
                       Class<T> objectType);

    <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String, Object> params,
                          Class<T> objectType, BiFunction<Class<T>, Map<String, Object>, T> mapper);

    Flux<Map<String, Object>> query(String query);

    ArcadeclientEntityConverter getConverter();

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
