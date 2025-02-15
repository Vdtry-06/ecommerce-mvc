package vdtry06.springboot.ecommerce.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    List<Product> findAllByIdInOrderById(List<Long> ids);

    List<Product> findByNameContaining(String name);

    void deleteByImageUrl(String imageUrl);
}
