package com.chubini.pku.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      // არ გაუშვა DB/JPA/Flyway/Security ავტოკონფიგები კონტექსტ-ლოდისთვის
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
    })
class PkuApiApplicationTests {

  @org.junit.jupiter.api.Test
  void contextLoads() {
    // just boot context
  }
}
