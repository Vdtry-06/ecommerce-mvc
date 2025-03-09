package vdtry06.springboot.ecommerce.address;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.address.dto.AddressRequest;
import vdtry06.springboot.ecommerce.address.dto.AddressResponse;
import vdtry06.springboot.ecommerce.user.User;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.user.UserRepository;
import vdtry06.springboot.ecommerce.user.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressService {
    AddressRepository addressRepository;
    AddressMapper addressMapper;
    UserRepository userRepository;
    UserService userService;

    @PostAuthorize("returnObject.username == authentication.name")
    public AddressResponse createAddress(AddressRequest request) {

//        log.info("Creating address for user {}", userId);
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getAddress() != null) {
            throw new AppException(ErrorCode.ADDRESS_ALREADY_EXISTS);
        } else {
            Address address = addressMapper.toAddress(request);

            address.setUser(user);
            user.setAddress(address);
            userRepository.save(user);
            return addressMapper.toAddressResponse(user.getAddress());
        }
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public AddressResponse getAddress(Long addressId) {
//        Long userId = getCurrentUserId();

        log.info("Getting address {} for user {}", addressId, userService.getCurrentUserId());

        Address address = addressRepository.findByIdAndUserId(addressId, userService.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER));

        return addressMapper.toAddressResponse(address);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public List<AddressResponse> getAddresses() {
        log.info("Getting all addresses");
        List<Address> addresses = addressRepository.findAll();

        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public AddressResponse updateAddress(AddressRequest request) {
        Long userId = userService.getCurrentUserId();

        log.info("Updating address for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Address address = user.getAddress();

        if(address == null) throw new AppException(ErrorCode.ADDRESS_NOT_EXISTED);

        if(request.getCountry() != null && !address.getCountry().equals(request.getCountry())) address.setCountry(request.getCountry());
        if(request.getCity() != null && !address.getCity().equals(request.getCity())) address.setCity(request.getCity());
        if(request.getDistrict() != null && !address.getDistrict().equals(request.getDistrict())) address.setDistrict(request.getDistrict());
        if(request.getWard() != null && !address.getWard().equals(request.getWard())) address.setWard(request.getWard());
        if(request.getStreet() != null && !address.getStreet().equals(request.getStreet())) address.setStreet(request.getStreet());
        if(request.getHouseNumber() != null && !address.getHouseNumber().equals(request.getHouseNumber())) address.setHouseNumber(request.getHouseNumber());

        log.info("Address updated for user {}: {}", userId, address);

        address = addressRepository.save(address);
        return addressMapper.toAddressResponse(address);
    }
}
