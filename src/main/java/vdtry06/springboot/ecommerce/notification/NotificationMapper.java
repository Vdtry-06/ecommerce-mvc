package vdtry06.springboot.ecommerce.notification;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.notification.dto.NotificationResponse;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toNotificationResponse(Notification notification);
}
