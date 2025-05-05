package vdtry06.springboot.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import vdtry06.springboot.ecommerce.dto.request.ChatbotRequest;
import vdtry06.springboot.ecommerce.dto.response.ChatbotResponse;
import vdtry06.springboot.ecommerce.service.ChatbotService;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public Mono<ResponseEntity<ChatbotResponse>> askQuestion(@RequestBody ChatbotRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(new ChatbotResponse("Text is required")));
        }

        return chatbotService.generateConciseResponse(request.getText())
                .map(response -> ResponseEntity.ok(new ChatbotResponse(response)))
                .defaultIfEmpty(ResponseEntity.ok(new ChatbotResponse("No response generated")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(new ChatbotResponse("Error processing request: " + e.getMessage()))));
    }
}
