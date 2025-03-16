package vdtry06.springboot.ecommerce.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContaining(String name);

    boolean existsByName(String name);

    Optional<Category> findByName(String name);


}
