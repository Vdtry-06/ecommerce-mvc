package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.address.AddressRequest;
import vdtry06.springboot.ecommerce.dto.response.address.AddressResponse;
import vdtry06.springboot.ecommerce.entity.Address;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.AddressMapper;
import vdtry06.springboot.ecommerce.repository.AddressRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressService {
    AddressRepository addressRepository;
    AddressMapper addressMapper;
    UserRepository userRepository;

    public AddressResponse createAddress(AddressRequest request) {
        Long userId = getCurrentUserId();

        log.info("Creating address for user {}", userId);
        User user = userRepository.findById(userId)
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

    public AddressResponse getAddress(Long addressId) {
        Long userId = getCurrentUserId();

        log.info("Getting address {} for user {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
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

    public AddressResponse updateAddress(AddressRequest request) {
        Long userId = getCurrentUserId();

        log.info("Updating address for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Address address = user.getAddress();

        if(address == null) throw new AppException(ErrorCode.ADDRESS_NOT_EXISTED);

        if(request.getStreet() != null && !address.getStreet().equals(request.getStreet())) address.setStreet(request.getStreet());
        if(request.getHouseNumber() != null && !address.getHouseNumber().equals(request.getHouseNumber())) address.setHouseNumber(request.getHouseNumber());
        if(request.getZipCode() != null && !address.getZipCode().equals(request.getZipCode())) address.setZipCode(request.getZipCode());

        log.info("Address updated for user {}: {}", userId, address);

        address = addressRepository.save(address);
        return addressMapper.toAddressResponse(address);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("Getting current user id {}", user.getId());
        return user.getId();
    }

}
