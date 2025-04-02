package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.mapper.ToppingMapper;
import vdtry06.springboot.ecommerce.repository.ToppingRepository;
import vdtry06.springboot.ecommerce.dto.request.ToppingRequest;
import vdtry06.springboot.ecommerce.dto.response.ToppingResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ToppingService {
    ToppingMapper toppingMapper;
    ToppingRepository toppingRepository;


    @PreAuthorize("hasRole('ADMIN')")
    public ToppingResponse addTopping(ToppingRequest request) {
        if(toppingRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.TOPPING_NAME_EXISTS);
        }

        Topping topping = toppingMapper.toTopping(request);
        topping.setName(request.getName());
        topping.setPrice(request.getPrice());
        toppingRepository.save(topping);

        return toppingMapper.toToppingResponse(topping);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ToppingResponse updateTopping(Long id, ToppingRequest request) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));

        if(request.getName() != null && !request.getName().isEmpty()) {
            topping.setName(request.getName());
        }
        if(request.getPrice() != null) {
            topping.setPrice(request.getPrice());
        }
        toppingRepository.save(topping);

        return toppingMapper.toToppingResponse(topping);
    }

    public ToppingResponse getTopping(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));

        return toppingMapper.toToppingResponse(topping);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTopping(Long id) {
        toppingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));
        toppingRepository.deleteById(id);
    }

    public List<ToppingResponse> getAllToppings() {
        return toppingRepository.findAll().stream()
                .map(toppingMapper::toToppingResponse)
                .collect(Collectors.toList());
    }
}
