package com.worldtaletalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class WorldTaleTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorldTaleTalkApplication.class, args);
    }

}
