package org.makkiato.arcadeclient.data.core;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.reactivestreams.Publisher;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SimpleArcadedbRepository<T extends IdentifiableDocumentBase, ID> implements ArcadedbCrudRepository<T> {

    private final ArcadedbOperations operations;
    private final Class<T> domainType;
    private final String documentName;

    public SimpleArcadedbRepository(ArcadedbOperations operations, Class<T> domainType) {
        Assert.notNull(operations, "ArcadedbOperations must not be null");
        Assert.notNull(domainType, "Domain Type must not be null");
        this.operations = operations;
        this.domainType = domainType;
        var typeNameAnnotation = domainType.getAnnotation(JsonTypeName.class);
        this.documentName = typeNameAnnotation != null ? typeNameAnnotation.value() : domainType.getSimpleName();
    }

    @Override
    public <S extends T> Mono<S> save(S entity) {
        Assert.notNull(entity, "Entity must not be null");
        return operations.mergeDocument(entity);
    }

    @Override
    public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");
        return Flux.fromIterable(entities).flatMap(this::save);
    }

    @Override
    public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
        Assert.notNull(entityStream, "The given Publisher of entities must not be null");
        return Flux.from(entityStream).flatMap(this::save);
    }

    @Override
    public Mono<T> findById(String id) {
        Assert.notNull(id, "The given id must not be null");
        return operations.findById(id, domainType);
    }

    @Override
    public Mono<T> findById(Publisher<String> publisher) {
        Assert.notNull(publisher, "The given id must not be null");
        return Mono.from(publisher).flatMap(this::findById);
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        Assert.notNull(id, "The given id must not be null");
        return operations.exists(id);
    }

    @Override
    public Mono<Boolean> existsById(Publisher<String> publisher) {
        Assert.notNull(publisher, "The given id must not be null");
        return Mono.from(publisher).flatMap(this::existsById);
    }

    @Override
    public Flux<T> findAll() {
        var command = String.format("select from %s", documentName);
        return operations.selectDocument(command, domainType);
    }

    @Override
    public Flux<T> findAllById(Iterable<String> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null");
        return Flux.fromIterable(ids).flatMap(this::findById);
    }

    @Override
    public Flux<T> findAllById(Publisher<String> ids) {
        Assert.notNull(ids, "The given Publisher of Id's must not be null");
        return Flux.from(ids).flatMap(this::findById);
    }

    @Override
    public Mono<Long> count() {
        return operations.count(documentName);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        Assert.notNull(id, "The given id must not be null");
        return operations.deleteById(id, documentName);
    }

    @Override
    public Mono<Void> deleteById(Publisher<String> publisher) {
        Assert.notNull(publisher, "The given id must not be null");
        return Mono.from(publisher).flatMap(this::deleteById);
    }

    @Override
    public Mono<Void> delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null");
        return operations.delete(entity);
    }

    @Override
    public Mono<Void> deleteAllById(Iterable<? extends String> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null");
        return operations.deleteAllById(ids, documentName);
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");
        var ids = StreamSupport.stream(entities.spliterator(), false)
                .map(IdentifiableDocumentBase::getRid).collect(Collectors.toUnmodifiableList());
        return deleteAllById(ids);
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {
        Assert.notNull(entityStream, "The given Publisher of entities must not be null");
        return Flux.from(entityStream).flatMap(this::delete).last();
    }

    @Override
    public Mono<Void> deleteAll() {
        var yes = operations.deleteAll(documentName);
        return yes;
    }

}
