package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.OrderLine;

import java.util.List;
import java.util.Optional;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    List<OrderLine> findAllByOrderId(Long orderId);

    List<OrderLine> findByOrder(Order order);

    Optional<OrderLine> findByOrderIdAndProductId(Long orderId, Long orderId1);

    void deleteByOrder(Order order);
}
