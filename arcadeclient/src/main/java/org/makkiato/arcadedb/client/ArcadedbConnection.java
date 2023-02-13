package org.makkiato.arcadedb.client;

import org.makkiato.arcadedb.client.httpexchange.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.Record;
import java.util.Map;

public record ArcadedbConnection(String name, WebClient webClient) {

    public Boolean close() {
        var result = new ServerExchange("sql", String.format("close database %s", name), webClient)
                .exchange().block().result();
        return(result.equalsIgnoreCase("ok"));
    }

    public Map<String, String>[] command(String command) {
        return new CommandExchange("sql", command, name, webClient)
                .exchange().block().result();
    }
}