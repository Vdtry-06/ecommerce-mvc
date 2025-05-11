package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.entity.Review;
import vdtry06.springboot.ecommerce.entity.User;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserAndProduct(User user, Product product);

    List<Review> findByProduct(Product product);

    List<Review> findByUser(User user);

    List<Review> findByProductAndVisible(Product product, boolean visible);
}
