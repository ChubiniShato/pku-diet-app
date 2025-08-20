package com.chubini.pku.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.chubini.pku")
@EntityScan("com.chubini.pku.products")
@EnableJpaRepositories("com.chubini.pku.products")
public class PkuApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkuApiApplication.class, args);
    }
}
