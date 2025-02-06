package vdtry06.springboot.ecommerce.orderline;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.order.Order;
import vdtry06.springboot.ecommerce.product.Product;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "order_lines")
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    Integer quantity;
    BigDecimal price;
}
