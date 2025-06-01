package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vdtry06.springboot.ecommerce.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartSyncScheduler {
    CartService cartService;
    UserRepository userRepository;

    @Scheduled(fixedRate = 300000)
    public void syncCarts() {
        userRepository.findAll().forEach(user -> {
            cartService.syncCartToDatabase(user.getId());
        });
        log.info("Completed scheduled cart sync");
    }
}
