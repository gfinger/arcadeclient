package org.makkiato.arcadeclient.example;

import org.makkiato.arcadeclient.data.repository.EnableArcadeclientRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableArcadeclientRepositories
public class Application {

    public static void main(String[] args) {
        var context = SpringApplication.run(Application.class);
    }
}