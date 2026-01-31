package sunmight.openai.sunmight_ai.domain.tools;

import org.springframework.ai.tool.annotation.Tool;
import sunmight.openai.sunmight_ai.api.dto.UserResponseDTO;

// TODO:: 날짜 시간별 설비별 작업조별 설비 온도별 품질별 조회 -> 날짜 시간별 설비별 작업조별 설비 온도별 품질별 품질 원인 분석
public class ChatTools {

    /**
     * @Tool(description) 을 통해서 LLM api에서 요청한 튤을 찾고 실행함
     * @return
     */
    @Tool(description = "User personal information : name, age, address, phone, etc")
    public UserResponseDTO getUserInfoTool() {
        // service 단에서 유저의 이름을 찾는 로직을 호출하여 응답하도록 활용 가능.
        return new UserResponseDTO("김영진", 29L, "안산시 단원구", "010-7777-7777", "zipCode는 없음");
    }

}
