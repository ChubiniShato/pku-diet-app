package com.chubini.pku.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    // არ გაუშვა DB/JPA/Flyway ავტოკონფიგები კონტექსტ-ლოდისთვის
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class PkuApiApplicationTests {

    @org.junit.jupiter.api.Test
    void contextLoads() {
        // just boot context
    }
}
