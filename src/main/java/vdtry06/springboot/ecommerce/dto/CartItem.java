package vdtry06.springboot.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("quantity")
    Integer quantity;

    @JsonProperty("selected")
    Boolean selected;

    @JsonProperty("toppingIds")
    Set<Long> toppingIds;
}
