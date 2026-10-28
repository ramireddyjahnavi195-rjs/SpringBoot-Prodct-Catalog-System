
# Spring Boot Product Catalog System

A REST API for managing products using Java, Spring Boot,
Spring MVC, Maven, Spring Data JPA and H2.

## Features

- Create, read, update and delete products
- Input validation
- Standardized error responses
- H2 database integration
- Sample product data
- Automated controller tests
- Postman API testing

## Requirements

- Java 25 LTS or later
- Maven
- Postman (for manual API testing)

## Run the project

Run tests:

    mvn clean test

Start the application:

    mvn spring-boot:run

Base URL:

    http://localhost:8080/api/products

## Endpoints

| Method | Endpoint | Purpose |
| POST | /api/products | Create product |
| GET | /api/products | List products |
| GET | /api/products/{id} | Get one product |
| PUT | /api/products/{id} | Update product |
| DELETE | /api/products/{id} | Delete product |

## H2 Console

Open http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:catalogdb
Username: sa
Password: blank

## Database

The H2 database is in memory. Data is lost when the application
stops. The setup is intended for local development and learning.
