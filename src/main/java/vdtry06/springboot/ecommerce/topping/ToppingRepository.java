package vdtry06.springboot.ecommerce.topping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {
    boolean existsByName(String name);

    Optional<Topping> findByName(String name);
}
