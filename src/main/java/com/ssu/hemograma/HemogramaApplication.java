package com.ssu.hemograma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// ignorar banco de dados por enquanto, quando necessário retirar o exclude da anotação
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class HemogramaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HemogramaApplication.class, args);
    }
}