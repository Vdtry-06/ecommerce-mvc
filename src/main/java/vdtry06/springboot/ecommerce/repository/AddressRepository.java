package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.entity.Address;
import vdtry06.springboot.ecommerce.entity.User;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUser(User user);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

}
