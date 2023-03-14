package org.makkiato.arcadedb.client.web.client;

import java.util.List;
import java.util.function.Function;

import org.springframework.web.reactive.function.client.WebClient;

public interface WebClientSupplierStrategy extends Function<List<WebClientSpec>, WebClient>{}
