package vdtry06.springboot.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    Long productId;
    String productName;
    BigDecimal price;
    String productImageUrl;
    String description;

    @JsonProperty("quantity")
    Integer quantity;

    @JsonProperty("selected")
    Boolean selected;

    @JsonProperty("toppingIds")
    Set<Long> toppingIds;
}
