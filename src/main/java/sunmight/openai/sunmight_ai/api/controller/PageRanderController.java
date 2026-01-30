package sunmight.openai.sunmight_ai.api.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sunmight.openai.sunmight_ai.domain.entity.ChatEntity;
import sunmight.openai.sunmight_ai.domain.service.ChatService;

@RequiredArgsConstructor
@Controller
public class PageRanderController {

    private final ChatService chatService;

    @GetMapping("/")
    public String streamChatPage() {
        return "chat";
    }

    @ResponseBody
    @PostMapping("/chat/history/{userid}")
    public List<ChatEntity> getChatHistory(@PathVariable("userid") String userId) {
        return chatService.readAllChats(userId);
    }
}
