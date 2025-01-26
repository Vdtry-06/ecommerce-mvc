package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContaining(String name);

    boolean existsByName(String name);
}
