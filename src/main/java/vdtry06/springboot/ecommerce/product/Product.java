package vdtry06.springboot.ecommerce.product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.category.Category;
import vdtry06.springboot.ecommerce.orderline.OrderLine;
import vdtry06.springboot.ecommerce.topping.Topping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@ToString
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    String imageUrl;

    @Column(nullable = false)
    Integer availableQuantity;

    BigDecimal price;

    @ManyToMany
    @JoinTable(
            name = "product_topping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    Set<Topping> toppings;

    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    Set<Category> categories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderLine> orderLines;
}
