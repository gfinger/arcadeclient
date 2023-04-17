package org.makkiato.arcadeclient.data.web.client;

import org.springframework.web.reactive.function.client.WebClient;

public record WebClientSpec(WebClient webClient, boolean ha, boolean leader, boolean replica) {
}
