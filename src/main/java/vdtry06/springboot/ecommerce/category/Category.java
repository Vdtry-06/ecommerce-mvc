package vdtry06.springboot.ecommerce.category;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.product.Product;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String description;

    @ManyToMany(mappedBy = "categories")
    Set<Product> products;
}
