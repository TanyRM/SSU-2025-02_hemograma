package com.ssu.hemograma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class HemogramaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HemogramaApplication.class, args);
    }

}