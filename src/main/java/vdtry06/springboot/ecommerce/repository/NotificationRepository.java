package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vdtry06.springboot.ecommerce.constant.NotificationType;
import vdtry06.springboot.ecommerce.entity.Notification;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByOrderIdAndType(Long orderId, NotificationType type);
}
