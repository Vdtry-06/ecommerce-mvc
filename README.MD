# 🏍️ E-Commerce Platform

A comprehensive and scalable web-based **e-commerce platform** built with **Spring Boot**, supporting full shopping experiences including authentication, product & order management, payment integration, chatbot support, and containerized deployment.

---

## 📚 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Objectives](#2-objectives)
3. [System Architecture](#3-system-architecture)
4. [Key Features](#4-key-features)
5. [Installation & Setup](#5-installation--setup)
6. [API Documentation](#6-api-documentation)
7. [Challenges & Solutions](#7-challenges--solutions)
8. [Future Improvements](#8-future-improvements)
9. [Frontend](#9-frontend)
10. [Conclusion](#10-conclusion)
---

## 1. Project Overview

**E-Commerce Platform** is a full-stack web application developed using **Spring Boot**, featuring:

* A backend RESTful API for shopping, authentication, payments, and more
* Integration with external services like **Redis**, **Kafka**, **VNPay**, **ZaloPay**, **Cloudinary**, **Gmail SMTP**, and **Gemini Chatbot**
* OAuth2 authentication via **Google** and **GitHub**
* A scalable and secure backend platform for real-world e-commerce systems

---

## 2. Objectives

* ✅ Build a powerful backend for e-commerce using REST APIs
* ⚡ Improve scalability with **Redis** (for caching) and **Kafka** (for async messaging)
* 🔐 Implement **JWT**, **OAuth2**, and **RBAC** security
* 💳 Support secure payments through **VNPay** and **ZaloPay**
* 🤖 Integrate **Gemini API chatbot** for customer support
* 📩 Send transaction emails via **Spring Mail**

---

## 3. System Architecture

### 3.1 Technologies Used

| Component      | Technology                            |
| -------------- | ------------------------------------- |
| Backend        | Spring Boot 3.3.7 (Java 21)           |
| Database       | MySQL 8.0 (JPA/Hibernate)             |
| Caching        | Redis 7                               |
| Messaging      | Apache Kafka                          |
| Authentication | Spring Security + JWT + OAuth2        |
| Payments       | VNPay, ZaloPay                        |
| Chatbot        | Gemini API (via Spring AI)            |
| Emails         | Spring Mail (Gmail SMTP)              |
| Image Hosting  | Cloudinary                            |
| Containers     | Docker & Docker Compose               |
| Build Tool     | Maven                                 |
| Others         | Lombok, MapStruct, Thymeleaf, OpenAPI |

### 3.2 System Components

Project folder structure:

* `config/` – Spring configurations
* `constant/` – Static constants used across the project
* `controller/` – REST controllers
* `dto/` – Data Transfer Objects
* `entity/` – JPA entities representing DB tables
* `exception/` – Custom exception handling
* `mapper/` – MapStruct mappers
* `repository/` – JPA repositories (DAO layer)
* `service/` – Business logic services
* `validator/` – Custom input validators

### 3.3 Database Schema

Main entities:

* `User`, `Address`, `Role`, `Permission`
* `Product`, `Category`, `Topping`, `Review`
* `Cart`, `Order`, `OrderLine`, `Payment`
* `Notification`

### 3.4 External Integrations

* **Cloudinary** – for product image hosting
* **Kafka** – for event publishing (cart, payment)
* **Redis** – for caching cart/session data
* **VNPay / ZaloPay** – for payment processing
* **Gmail SMTP** – for email delivery
* **Gemini API** – for AI chatbot support

---

## 4. Key Features

### 4.1 👥 User Management

* Email verification upon registration
* OAuth2 login (Google/GitHub)
* Profile update, password reset, order history

### 4.2 🛆 Product Management

* CRUD operations (admin only)
* Filter by category, toppings
* Search by product name

### 4.3 🛒 Shopping Cart

* Guest and authenticated carts (stored in Redis)
* Merge guest cart after login

### 4.4 📁 Order Management

* Create & view orders
* Track order status: `PENDING`, `PAID`, `DELIVERED`, `CANCELLED`

### 4.5 💳 Payment Processing

* VNPay & ZaloPay integration
* Handle callback & email confirmation
* Cancel payment and clean up cart/Redis

### 4.6 ✍️ Reviews

* Add/edit/delete product reviews (user)
* Admin moderation

### 4.7 🤖 Chatbot

* Ask questions using Gemini AI chatbot

### 4.8 🔔 Notifications

* Emails: verification, confirmation, cart expiration
* Kafka: async event publishing for cart/payment

### 4.9 🔐 Security

* JWT auth with token invalidation (logout)
* OAuth2 login via Google/GitHub
* Role-Based Access Control (RBAC)

---

## 5. Installation & Setup

### 5.1 Prerequisites

* Java 21
* Maven 3.9.9
* Docker & Docker Compose
* MySQL 8.0, Redis 7, Kafka
* `.env` file containing credentials (Cloudinary, VNPay, ZaloPay, Gmail, Gemini)

### 5.2 Setup Steps

```bash
# Clone repository
git clone https://github.com/Vdtry-06/ecommerce-mvc.git
cd ecommerce

# Create environment file
make env

# Edit .env with credentials

# Build project
make build

# Build Docker image
make docker-build

# Start the application
make up
```

### 5.3 Access Points

* **API Base**: [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
* **Swagger UI**: [http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)

### 5.4 Useful Commands

```bash
mingw32-make up       # Start the system
mingw32-make logs     # View logs
mingw32-make down     # Stop containers
mingw32-make clean    # Cleanup Docker resources
mingw32-make test     # Run unit tests
```

---

## 6. API Documentation

### 6.1 🔐 Authentication

* `POST /auth/signup` – Register
* `POST /auth/login` – Login
* `POST /auth/verify` – Email verification
* `POST /auth/refresh` – Refresh token
* `GET /auth/oauth2/success` – OAuth2 callback

### 6.2 👤 User Management

* `PUT /auth/users/{id}` – Update profile
* `GET /auth/users/myInfo` – View personal info
* `GET /auth/users/all` – List all users (admin)

### 6.3 🏍️ Product Management

* `POST /product/add` – Add product
* `GET /product/get-all` – List products
* `GET /product/categories/filter` – Filter by category

### 6.4 🛒 Cart Management

* `POST /cart/{userId}/add` – Add item
* `PUT /cart/{userId}/update` – Update item
* `GET /cart/{userId}` – Get cart
* `POST /cart/guest/add` – Add guest cart

### 6.5 🚚 Order Management

* `POST /orders` – Create order
* `GET /orders/user-id/{userId}` – Get user orders
* `GET /orders/{orderId}` – Order details

### 6.6 💳 Payment

* `POST /payment/vn-pay-selected` – Initiate VNPay
* `GET /payment/vn-pay-callback` – VNPay callback
* `PUT /payment/{orderId}/cancel` – Cancel payment

### 6.7 🤖 Chatbot

* `POST /chatbot/ask` – Ask chatbot

### 6.8 ✍️ Reviews

* `POST /reviews/add` – Add review
* `GET /reviews/product/{productId}` – Get product reviews

---

## 7. Challenges & Solutions

| Challenge                        | Solution                                                 |
| -------------------------------- | -------------------------------------------------------- |
| Guest cart management            | Store via Redis sessions; merge after login              |
| Secure payment callback handling | Validate transaction references; wrap in DB transactions |
| Async notification system        | Use Kafka for cart/payment events                        |
| JWT logout token invalidation    | Track invalidated tokens in DB                           |

---

## 8. Future Improvements

* Split into microservices for scalability
* Add Elasticsearch for full-text search
* Build ML-based recommendation engine
* Add i18n (multi-language support)
* Implement rate limiting
* Integrate Prometheus + Grafana for monitoring

---

## 9. Frontend
```
Link: https://github.com/Vdtry-06/react-ecommerce
git clone: https://github.com/Vdtry-06/react-ecommerce.git
```

## 10. Conclusion
```
This E-Commerce Platform offers a modern, secure, and extensible backend for online shopping. With strong integrations, clean architecture, and future-ready features, it delivers a solid foundation for developers and businesses alike.
```
---

**Contact:** [tunhoipro0306@gmail.com](mailto:tunhoipro0306@gmail.com)
**© 2025 E-Commerce Platform. All rights reserved.**
