package org.makkiato.arcadeclient.example;

import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.makkiato.arcadeclient.data.core.ArcadedbTemplate;
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
}
