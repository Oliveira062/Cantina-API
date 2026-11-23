package br.edu.cantina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CantinaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CantinaApiApplication.class, args);
    }
}
