package org.makkiato.arcadeclient.example;

import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.makkiato.arcadeclient.data.operations.GenericOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
public class DatabaseInitializer {
    private final ArcadedbFactory factory;
    private final ArcadedbTemplate template;
    @Value("classpath:customer-schema.sql")
    private Resource schema;
    @Value("classpath:customer-data.sql")
    private Resource data;
    @Value("classpath:types.graphql")
    private Resource types;

    public DatabaseInitializer(ArcadedbFactory factory, ArcadedbTemplate template) {
        this.factory = factory;
        this.template = template;
    }

    public Mono<Boolean> schema() {
        return factory.exists().flatMap(exists -> {
            if(exists) {
                return factory.drop();
            } else {
                return Mono.just(true);
            }
        }).flatMap(unused -> factory.create()).flatMap(unused -> {
            try {
                return template.script(schema);
            } catch (IOException ex) {
                return Mono.error(ex);
            }
        });
    }

    public Mono<Boolean> data() {
        try {
            return template.script(data);
        } catch (IOException ex) {
            return Mono.error(ex);
        }
    }

    public Mono<Boolean> types() {
        try {
            return template.script(GenericOperations.CommandLanguage.GRAPHQL, types, null);
        } catch (IOException ex) {
            return Mono.error(ex);
        }
    }
}
