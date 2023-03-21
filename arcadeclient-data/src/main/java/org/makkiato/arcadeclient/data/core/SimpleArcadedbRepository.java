package org.makkiato.arcadeclient.data.core;

import org.reactivestreams.Publisher;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SimpleArcadedbRepository<T extends DocumentBase, ID> implements ArcadedbCrudRepository<T, ID> {

    private final ArcadedbOperations operations;

    public SimpleArcadedbRepository(ArcadedbOperations operations) {
        Assert.notNull(operations, "ArcadedbOperations must not be null");
        this.operations = operations;
    }

    @Override
    public <S extends T> Mono<S> save(S entity) {
        Assert.notNull(entity, "Entity must not be null");
        return operations.mergeDocument(entity);
    }

    @Override
    public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
    }

    @Override
    public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
        Assert.notNull(entityStream, "The given Publisher of entities must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
    }

    @Override
    public Mono<T> findById(ID id) {
        Assert.notNull(id, "The given id must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Mono<T> findById(Publisher<ID> publisher) {
        Assert.notNull(publisher, "The given id must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Mono<Boolean> existsById(ID id) {
        Assert.notNull(id, "The given id must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsById'");
    }

    @Override
    public Mono<Boolean> existsById(Publisher<ID> publisher) {
        Assert.notNull(publisher, "The given id must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsById'");
    }

    @Override
    public Flux<T> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public Flux<T> findAllById(Iterable<ID> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllById'");
    }

    @Override
    public Flux<T> findAllById(Publisher<ID> ids) {
        Assert.notNull(ids, "The given Publisher of Id's must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllById'");
    }

    @Override
    public Mono<Long> count() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }

    @Override
    public Mono<Void> deleteById(ID id) {
        Assert.notNull(id, "The given id must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    @Override
    public Mono<Void> deleteById(Publisher<ID> publisher) {
        Assert.notNull(publisher, "The given id must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    @Override
    public Mono<Void> delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public Mono<Void> deleteAllById(Iterable<? extends ID> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllById'");
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {
        Assert.notNull(entityStream, "The given Publisher of entities must not be null");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
    }

    @Override
    public Mono<Void> deleteAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAll'");
    }

}
