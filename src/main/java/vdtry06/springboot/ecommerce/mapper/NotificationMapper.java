package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.dto.response.notification.NotificationResponse;
import vdtry06.springboot.ecommerce.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toNotificationResponse(Notification notification);
}
