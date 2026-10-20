package com.example.demoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Start konfiguracji Boot i skanowania komponentów w tym pakiecie i podpakietach.
public class DemoappApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoappApplication.class, args);
    }

}
