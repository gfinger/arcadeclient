package org.makkiato.arcadeclient.data.core;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

public interface ArcadedbOperations {
    Mono<Boolean> script(Resource resource) throws IOException;

    Mono<Boolean> script(Resource resource, Map<String, Object> params)
            throws IOException;

    Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params) throws IOException;

    Mono<Boolean> script(String[] commands);

    Mono<Boolean> script(String[] commands, Map<String, Object> params);

    Flux<Map<String, Object>> command(String command);

    Flux<Map<String, Object>> command(String command, Map<String, Object> params);

    Flux<Map<String, Object>> command(CommandLanguage language, String command, Map<String, Object> params);

    @SuppressWarnings("unchecked")
    <T> Mono<T> insertDocument(String documentName, T object);

    <T extends DocumentBase> Mono<T> insertDocument(T object);

    Mono<Boolean> exists(String rid);

    <T> Mono<Map<String, Object>> updateDocument(String rid, T object);

    <T extends DocumentBase> Mono<T> updateDocument(T object);

    <T extends DocumentBase> Mono<T> mergeDocument(T object);

    <T> Flux<T> selectDocument(String command, Class<T> objectType);

    <T> Flux<T> selectDocument(String command, Map<String, Object> params, Class<T> objectType);

    <T> Flux<T> selectDocument(CommandLanguage language, String command, Class<T> objectType);

    <T> Flux<T> selectDocument(CommandLanguage language, String command, Map<String, Object> params,
                               Class<T> objectType);

    <T> Flux<T> selectDocument(CommandLanguage language, String command, Map<String, Object> params,
                               Class<T> objectType, BiFunction<Class<T>, Map<String, Object>, T> mapper);

    Mono<Long> count(String documentName);

    Mono<Void> deleteById(String id, String documentName);

    <T extends DocumentBase> Mono<Void> delete(T document);

    Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentName);

    Mono<Void> deleteAll(String documentName);

    Flux<Map<String, Object>> query(String query);

    <T> Mono<T> findById(String rid, Class<T> objectType);
}
