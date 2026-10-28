
package com.example.catalogservice;

import com.example.catalogservice.entity.Product;
import com.example.catalogservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        Product product = new Product(
                "Laptop", "Student laptop",
                new BigDecimal("55000.00"), 10
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void shouldRejectInvalidProduct() throws Exception {
        Product product = new Product(
                "", "Invalid product",
                new BigDecimal("-10.00"), -1
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldSubmitNewProductToCreateRoute() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "action=\"/products\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"id\""))));

        var createResult = mockMvc.perform(post("/products")
                        .param("name", "Monitor")
                        .param("description", "HD monitor")
                        .param("price", "199.99")
                        .param("stockQuantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute(
                        "successMessage", "Product added successfully!"))
                .andReturn();

        org.assertj.core.api.Assertions.assertThat(repository.findAll())
                .extracting(Product::getName)
                .contains("Monitor");

        mockMvc.perform(get("/products").flashAttrs(createResult.getFlashMap()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Product added successfully!")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Monitor")));
    }

    @Test
    void shouldShowFormErrorsInsteadOfBadRequestPage() throws Exception {
        mockMvc.perform(post("/products")
                        .param("name", "Invalid price product")
                        .param("price", "-1")
                        .param("stockQuantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Please correct the highlighted fields and try again.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Price must be greater than zero")));
    }
}
