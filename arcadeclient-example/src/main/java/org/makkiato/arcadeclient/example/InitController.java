package org.makkiato.arcadeclient.example;

import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/init")
public class InitController {
    private DatabaseInitializer databaseInitializer;

    public InitController(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    @PostMapping("schema")
    public Mono<Boolean> schema() {
        return databaseInitializer.schema();
    }
}
