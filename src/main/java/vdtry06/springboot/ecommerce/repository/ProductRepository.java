package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vdtry06.springboot.ecommerce.entity.Category;
import vdtry06.springboot.ecommerce.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    List<Product> findAllByIdInOrderById(List<Long> ids);

    List<Product> findByNameContaining(String name);

    void deleteByImageUrl(String imageUrl);

    Page<Product> findByCategory(Category category, Pageable pageable);

}
