package sunmight.openai.sunmight_ai.api.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import sunmight.openai.sunmight_ai.domain.service.OpenAiService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/openai")
@RestController
public class OpenAiController {

    private final OpenAiService openAiService;

    @PostMapping("/chat")
    public String chat (@RequestBody Map<String, String> body) {
        return openAiService.generate(body.get("text"));
    }

    @PostMapping("/chat/stream")
    public Flux<String> streamChat (@RequestBody Map<String, String> body) {
        return openAiService.generateStream(body.get("text"));
    }
}
