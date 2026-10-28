package com.example.catalogservice.controller;

import com.example.catalogservice.entity.Product;
import com.example.catalogservice.repository.ProductRepository;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {

    private final ProductRepository productRepository;

    // Correct constructor name
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Display products with search, stock, and price filters
    @GetMapping({"/", "/products"})
    public String showProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "all") String stock,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model) {

        List<Product> products = productRepository.findAll();

        if (q != null && !q.trim().isEmpty()) {
            String searchText = q.trim().toLowerCase(Locale.ROOT);

            products = products.stream()
                    .filter(product ->
                            (product.getName() != null
                                    && product.getName().toLowerCase(Locale.ROOT)
                                            .contains(searchText))
                            || (product.getDescription() != null
                                    && product.getDescription().toLowerCase(Locale.ROOT)
                                            .contains(searchText)))
                    .collect(Collectors.toList());
        }

        if ("inStock".equalsIgnoreCase(stock)) {
            products = products.stream()
                    .filter(product -> product.getStockQuantity() > 0)
                    .collect(Collectors.toList());
        } else if ("outOfStock".equalsIgnoreCase(stock)) {
            products = products.stream()
                    .filter(product -> product.getStockQuantity() == 0)
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() != null
                            && product.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() != null
                            && product.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("stock", stock);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "products";
    }

    // Display the form for adding a product
    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("pageTitle", "Add Product");
        model.addAttribute("formAction", "/products");
        return "product-form";
    }

    // Save a new product
    @PostMapping("/products")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Product");
            model.addAttribute("formAction", "/products");
            return "product-form";
        }

        productRepository.save(product);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Product added successfully!");

        return "redirect:/products";
    }

    // Display the form for editing an existing product
    @GetMapping("/products/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return productRepository.findById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("pageTitle", "Edit Product");
                    model.addAttribute("formAction", "/products/" + id);
                    return "product-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "Product not found.");
                    return "redirect:/products";
                });
    }

    // Update an existing product
    @PostMapping("/products/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            product.setId(id);
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("formAction", "/products/" + id);
            return "product-form";
        }

        if (!productRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Product not found.");
            return "redirect:/products";
        }

        product.setId(id);
        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Product updated successfully!");

        return "redirect:/products";
    }

    // Delete a product
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Product deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Product not found.");
        }

        return "redirect:/products";
    }
}
