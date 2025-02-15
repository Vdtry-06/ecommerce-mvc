package vdtry06.springboot.ecommerce.address;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.address.dto.AddressRequest;
import vdtry06.springboot.ecommerce.address.dto.AddressResponse;
import vdtry06.springboot.ecommerce.core.ApiResponse;

import java.util.List;


@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressController {
    AddressService addressService;

    @PostMapping("/user-id/{userId}")
    public ApiResponse<AddressResponse> createAddress(@PathVariable Long userId, @RequestBody AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .data(addressService.createAddress(userId, request))
                .build();
    }

    @GetMapping("/{addressId}/user-id/{userId}")
    public ApiResponse<AddressResponse> getAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        return ApiResponse.<AddressResponse>builder()
                .data(addressService.getAddress(userId, addressId))
                .build();
    }

    // có thể phân trang nếu dữ liệu lớn
    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddresses() {
        return ApiResponse.<List<AddressResponse>>builder()
                .data(addressService.getAddresses())
                .build();
    }

    @PutMapping("update-address/user-id/{userId}")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable Long userId, @RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .data(addressService.updateAddress(userId, request))
                .build();
    }
}
