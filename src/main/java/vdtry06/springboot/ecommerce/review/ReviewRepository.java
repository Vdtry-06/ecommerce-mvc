package vdtry06.springboot.ecommerce.review;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.product.Product;
import vdtry06.springboot.ecommerce.user.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserAndProduct(User user, Product product);
}
