package com.thomasvitale.bookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
public class BookServiceApplication {

    @Autowired
    CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request ->
                    request
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                      //  .jwt(Customizer.withDefaults())
                      .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(customJwtAuthenticationConverter))

                )
                .build();
    }

}

record Book(String title) {}

@RestController
class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    @GetMapping("books")
    public List<Book> getBooks() {
        log.info("Returning list of books in the catalog");
        return List.of(
                new Book("His Dark Materials"),
                new Book("The Hobbit"),
                new Book("The Lord of the Rings"));
    }

}
