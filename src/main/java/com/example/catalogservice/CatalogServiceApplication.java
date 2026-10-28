
package com.example.catalogservice;

import com.example.catalogservice.entity.Product;
import com.example.catalogservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Product(
                        "Laptop",
                        "Student laptop",
                        new BigDecimal("55000.00"),
                        10
                ));

                repository.save(new Product(
                        "Wireless Mouse",
                        "Wireless USB mouse",
                        new BigDecimal("799.00"),
                        25
                ));

                repository.save(new Product(
                        "Keyboard",
                        "USB keyboard",
                        new BigDecimal("1299.00"),
                        15
                ));
            }
        };
    }
}
