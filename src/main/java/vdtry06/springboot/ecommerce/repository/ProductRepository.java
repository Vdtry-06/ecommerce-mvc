package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
}
