package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.notification.NotificationRequest;
import vdtry06.springboot.ecommerce.dto.request.payment.PaymentRequest;
import vdtry06.springboot.ecommerce.mapper.PaymentMapper;
import vdtry06.springboot.ecommerce.repository.PaymentRepository;
import vdtry06.springboot.ecommerce.service.kafka.KafkaProducerService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentService {
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;
    KafkaProducerService kafkaProducerService;

    public Long createPayment(PaymentRequest request) {
        var payment = paymentRepository.save(paymentMapper.toPayment(request));

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .orderReference(request.getOrderReference())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .userFirstName(request.getUser().getFirstName())
                .userLastName(request.getUser().getLastName())
                .userEmail(request.getUser().getEmail())
                .build();

        kafkaProducerService.sendNotification(notificationRequest);
        return payment.getId();
    }
}
