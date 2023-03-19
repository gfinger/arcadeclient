package org.makkiato.arcadedb.client;

import org.springframework.web.reactive.function.client.WebClient;

public record TransactionHandle(String transactionId, WebClient webClient) {

}
