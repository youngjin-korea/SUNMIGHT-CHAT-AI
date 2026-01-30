package sunmight.openai.sunmight_ai.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sunmight.openai.sunmight_ai.domain.entity.ChatEntity;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    // userId로 챗 찾고 생성 날짜시간 순으로 조회
    List<ChatEntity> findByUserIdOrderByCreatedAtAsc(String userId);
}