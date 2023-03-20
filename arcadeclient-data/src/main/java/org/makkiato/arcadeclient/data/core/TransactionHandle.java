package org.makkiato.arcadeclient.data.core;

import org.springframework.web.reactive.function.client.WebClient;

public record TransactionHandle(String transactionId, WebClient webClient) {

}
