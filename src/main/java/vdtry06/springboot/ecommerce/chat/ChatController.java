package vdtry06.springboot.ecommerce.chat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import vdtry06.springboot.ecommerce.core.ApiResponse;

import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    OllamaChatModel chatModel;

    @GetMapping("/ai/generate")
    public ApiResponse<Map<String,String>> generate(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return ApiResponse.<Map<String, String>>builder().data(Map.of("generation", this.chatModel.call(message))).build();
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
