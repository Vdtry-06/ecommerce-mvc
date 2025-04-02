package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vdtry06.springboot.ecommerce.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    List<Product> findAllByIdInOrderById(List<Long> ids);

    List<Product> findByNameContaining(String name);

    void deleteByImageUrl(String imageUrl);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id IN :categoryIds " +
            "GROUP BY p HAVING COUNT(DISTINCT c.id) = :size")
    List<Product> findProductsByExactCategories(@Param("categoryIds") List<Long> categoryIds,
                                                @Param("size") long size);

}
