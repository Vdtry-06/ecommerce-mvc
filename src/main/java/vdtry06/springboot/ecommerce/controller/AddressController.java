package vdtry06.springboot.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.AddressRequest;
import vdtry06.springboot.ecommerce.dto.response.AddressResponse;
import vdtry06.springboot.ecommerce.service.AddressService;

import java.util.List;


@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressController {
    AddressService addressService;

    @PostMapping
    public ApiResponse<AddressResponse> createAddress(@RequestBody AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .data(addressService.createAddress(request))
                .build();
    }

    @GetMapping("/{addressId}")
    public ApiResponse<AddressResponse> getAddress(@PathVariable Long addressId) {
        return ApiResponse.<AddressResponse>builder()
                .data(addressService.getAddress(addressId))
                .build();
    }

    // có thể phân trang nếu dữ liệu lớn
    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddresses() {
        return ApiResponse.<List<AddressResponse>>builder()
                .data(addressService.getAddresses())
                .build();
    }

    @PutMapping
    public ApiResponse<AddressResponse> updateAddress(@RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .data(addressService.updateAddress(request))
                .build();
    }
}
