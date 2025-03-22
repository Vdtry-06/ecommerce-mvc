package vdtry06.springboot.ecommerce.review;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.product.Product;
import vdtry06.springboot.ecommerce.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;

    Long ratingScore;
    String comment;
    LocalDateTime reviewDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
