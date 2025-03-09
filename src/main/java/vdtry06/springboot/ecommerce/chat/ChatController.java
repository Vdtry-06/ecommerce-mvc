package vdtry06.springboot.ecommerce.chat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import vdtry06.springboot.ecommerce.cache.CacheService;
import vdtry06.springboot.ecommerce.core.ApiResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    OllamaChatModel chatModel;
    CacheService cacheService;

    @Async
    @GetMapping("/ai/generate")
    public CompletableFuture<ApiResponse<Map<String, String>>> generate(@RequestParam(value = "message") String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (cacheService != null) {
                String cachedResponse = cacheService.getFromCache(message);
                if (cachedResponse != null) {
                    return ApiResponse.<Map<String, String>>builder()
                            .data(Map.of("generation", cachedResponse))
                            .build();
                }
            }

            String response = chatModel.call(message);
            if (cacheService != null) {
                cacheService.saveToCache(message, response);
            }
            return ApiResponse.<Map<String, String>>builder()
                    .data(Map.of("generation", response))
                    .build();
        });
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
