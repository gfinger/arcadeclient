package org.makkiato.arcadedb.client.web;

import org.makkiato.arcadedb.client.exception.server.CommandExecutionException;
import org.makkiato.arcadedb.client.exception.server.ConcurrentModificationException;
import org.makkiato.arcadedb.client.exception.server.DatabaseIsClosedException;
import org.makkiato.arcadedb.client.exception.server.DatabaseOperationException;
import org.makkiato.arcadedb.client.exception.server.DuplicatedKeyException;
import org.makkiato.arcadedb.client.exception.server.IllegalArgumentException;
import org.makkiato.arcadedb.client.exception.server.NoSucheElementException;
import org.makkiato.arcadedb.client.exception.server.ParseException;
import org.makkiato.arcadedb.client.exception.server.QuorumNotReachedException;
import org.makkiato.arcadedb.client.exception.server.RemoteException;
import org.makkiato.arcadedb.client.exception.server.SchemaException;
import org.makkiato.arcadedb.client.exception.server.SecurityException;
import org.makkiato.arcadedb.client.exception.server.ServerIsNotTheLeaderException;
import org.makkiato.arcadedb.client.exception.server.TimeoutException;
import org.makkiato.arcadedb.client.exception.server.TransactionException;
import org.makkiato.arcadedb.client.exception.server.ValidationException;
import org.makkiato.arcadedb.client.web.response.ErrorResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ArcadedbErrorResponseFilterImpl implements ArcadedbErrorResponseFilter {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request).flatMap(ArcadedbErrorResponseFilterImpl::exchangeFilterResponseProcessor);
    }

    private static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
        var status = response.statusCode();
        if (!status.is2xxSuccessful()) {
            return response.bodyToMono(ErrorResponse.class).flatMap(error -> {
                log.error(String.format("ArcadeDB error: %s", error));
                return switch (error.getException()) {
                    case "com.arcadedb.network.binary.ServerIsNotTheLeaderException" ->
                        Mono.error(new ServerIsNotTheLeaderException(error.getError(),
                                status.value()));
                    case "com.arcadedb.network.binary.QuorumNotReachedException" ->
                        Mono.error(new QuorumNotReachedException(error.getError(),
                                status.value()));
                    case "com.arcadedb.exception.DuplicatedKeyException" ->
                        Mono.error(new DuplicatedKeyException(error.getDetail(),
                                status.value()));
                    case "com.arcadedb.exception.ConcurrentModificationException" ->
                        Mono.error(new ConcurrentModificationException(error.getError(),
                                status.value()));
                    case "com.arcadedb.exception.TransactionException" ->
                        Mono.error(new TransactionException(error.getError(), status.value()));
                    case "com.arcadedb.exception.TimeoutException" ->
                        Mono.error(new TimeoutException(error.getError(), status.value()));
                    case "com.arcadedb.exception.SchemaException" ->
                        Mono.error(new SchemaException(error.getDetail(), status.value()));
                    case "java.util.NoSuchElementException",
                            "com.arcadedb.server.security.ServerSecurityException" ->
                        Mono.error(new NoSucheElementException(error.getError(),
                                status.value()));
                    case "java.lang.SecurityException" ->
                        Mono.error(new SecurityException(error.getError(), status.value()));
                    case "com.arcadedb.query.sql.parser.ParseException" ->
                        Mono.error(new ParseException(error.getDetail(), status.value()));
                    case "com.arcadedb.exception.DatabaseOperationException" ->
                        Mono.error(new DatabaseOperationException(error.getDetail(),
                                status.value()));
                    case "java.lang.IllegalArgumentException" ->
                        Mono.error(new IllegalArgumentException(error.getDetail(),
                                status.value()));
                    case "com.arcadedb.exception.CommandExecutionException" ->
                        Mono.error((new CommandExecutionException(error.getDetail(),
                                status.value())));
                    case "com.arcadedb.exception.ValidationException" ->
                        Mono.error(new ValidationException(error.getDetail(), status.value()));
                    case "com.arcadedb.exception.DatabaseIsClosedException" ->
                        Mono.error(new DatabaseIsClosedException(error.getDetail(), status.value()));
                    default ->
                        Mono.error(new RemoteException(
                                String.format("Error on executing remote operation %s",
                                        error),
                                status.value()));
                };
            });
        }
        return Mono.just(response);
    }
}
