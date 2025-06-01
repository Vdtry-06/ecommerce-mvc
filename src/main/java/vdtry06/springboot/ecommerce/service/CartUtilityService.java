package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.ToppingRepository;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartUtilityService {
    ToppingRepository toppingRepository;

    public BigDecimal calculatePrice(Product product, Integer quantity, Set<Topping> toppings) {
        if (product.getPrice() == null || quantity == null) {
            log.error("Invalid price calculation inputs: product price: {}, quantity: {}", product.getPrice(), quantity);
            throw new AppException(ErrorCode.PRICE_CALCULATION_FAILED);
        }
        BigDecimal basePrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal toppingsPrice = toppings.stream()
                .map(topping -> topping.getPrice() != null ? topping.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(quantity));
        return basePrice.add(toppingsPrice);
    }

    public Set<Topping> validateToppings(Set<Long> toppingIds, Product product) {
        if (toppingIds == null || toppingIds.isEmpty()) {
            return Set.of();
        }
        Set<Topping> toppings = toppingRepository.findAllById(toppingIds).stream()
                .filter(topping -> product.getToppings().contains(topping))
                .collect(Collectors.toSet());
        if (toppings.size() != toppingIds.size()) {
            throw new AppException(ErrorCode.INVALID_TOPPING);
        }
        return toppings;
    }
}
