package sunmight.openai.sunmight_ai.domain.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunmight.openai.sunmight_ai.domain.entity.ChatEntity;
import sunmight.openai.sunmight_ai.domain.repository.ChatRepository;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<ChatEntity> readAllChats(String userId) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
