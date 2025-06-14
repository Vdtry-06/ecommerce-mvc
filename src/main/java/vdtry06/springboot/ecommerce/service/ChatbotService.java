package vdtry06.springboot.ecommerce.service;

import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    public ChatbotService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE)))
                .build();
    }

    public Mono<String> generateConciseResponse(String message) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();

        String enhancedPrompt = "Provide a very concise and direct answer to this question. " +
                "Be extremely brief and to the point. " +
                "Limit your response to 1-2 sentences maximum. " +
                "Do not use asterisks (*) or bullet points. " +
                "Do not ask follow-up questions. " +
                "Do not use markdown formatting. " +
                "If the question is not related to drinks then answer ask about drinks I will support you. " +
                "If ask about drinks then suggest drinks that customers want to drink. " +
                "Reply in Vietnamese. " +
                "Question: " + message;

        part.put("text", enhancedPrompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens", 100);
        generationConfig.put("temperature", 0.1);
        requestBody.put("generationConfig", generationConfig);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates == null || candidates.isEmpty()) {
                        throw new RuntimeException("No candidates found in response");
                    }

                    Map<String, Object> candidate = candidates.getFirst();
                    Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                    if (contentResponse == null) {
                        throw new RuntimeException("No content found in candidate");
                    }

                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResponse.get("parts");
                    if (parts == null || parts.isEmpty()) {
                        throw new RuntimeException("No parts found in content");
                    }

                    Map<String, Object> firstPart = parts.get(0);
                    String text = (String) firstPart.get("text");

                    return cleanupResponse(text);
                })
                .onErrorMap(e -> new RuntimeException("Failed to get response from Gemini API: " + e.getMessage()));
    }

    private String cleanupResponse(String text) {
        text = text.replace("*", "");
        text = text.replaceAll("\\s*\\*\\s+", "");
        text = text.replaceAll("\\s*\\d+\\.\\s+", "");
        text = text.replaceAll("#+\\s+", "");
        text = text.replace("_", "");
        text = text.replace("`", "");
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}
