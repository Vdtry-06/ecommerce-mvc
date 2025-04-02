package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
