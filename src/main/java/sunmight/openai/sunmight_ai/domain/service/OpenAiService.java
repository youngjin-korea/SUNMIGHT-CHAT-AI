package sunmight.openai.sunmight_ai.domain.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import sunmight.openai.sunmight_ai.api.dto.CityResponseDTO;
import sunmight.openai.sunmight_ai.domain.entity.ChatEntity;
import sunmight.openai.sunmight_ai.domain.repository.ChatRepository;
import sunmight.openai.sunmight_ai.domain.tools.ChatTools;

@RequiredArgsConstructor
@Service
public class OpenAiService {

    // elasticsearch를 붙힌 vectorstore
    private final VectorStore elasticsearchVectorStore;

    // JdbcChatMemoryRepository DI
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;

    private final OpenAiChatModel openAiChatModel;

    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    // 메세지 생성 + 옵션 생성 -> 프롬프트 생성 -> 모델 콜 -> 응답 text로 오픈
    public CityResponseDTO generate(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 메세지
        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(text);
        AssistantMessage assistantMessage = new AssistantMessage("");

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        //요청 및 응답 -> call() 호출시 entity 메소드로 outPut Convertor 가능
        return chatClient.prompt(prompt)
                .call()
                .entity(CityResponseDTO.class);  // TODO:: 보고서 양식의 DTO OutPut Structured 생성 (ex: 제목, 작성 날짜, 작성자, 분석내용 및 이유, 원인 및 이유, 해결책 및 이유)
    }

    public Flux<String> generateStream(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 유저&페이지별 ChatMemory를 관리하기 위한 key (우선은 명시적으로)
        String userId = "user1" + "_" + "1";

        // 전체 대화 db에 저장
        ChatEntity chatUserEntity = new ChatEntity();
        chatUserEntity.setUserId(userId);
        chatUserEntity.setType(MessageType.USER);
        chatUserEntity.setContent(text);

        // ChatMemory는 메모리 내의 대화상태를 관리, 실제 데이터의 영속성은 ChatMemoryRepository에서 담당
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(text)); // 신규 메세지 ChatMemoryRepository로 DB에 추가

        //옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        //프롬프트 -> message list, option
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        //응답 메세지를 담아줄 버퍼
        StringBuilder responseBuffer = new StringBuilder();

        Advisor ragAdvisor = QuestionAnswerAdvisor.builder(elasticsearchVectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build())
                .build();

        // 요청 및 응답
        return chatClient.prompt(prompt)
                .tools(new ChatTools())
                .advisors(ragAdvisor)
                .stream()
                .content()
                .map(token -> {
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    // chatMemory저장
                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

                    //전체 대화내용 저장
                    ChatEntity chatAssistantEntity = new ChatEntity();
                    chatAssistantEntity.setUserId(userId);
                    chatAssistantEntity.setType(MessageType.ASSISTANT);
                    chatAssistantEntity.setContent(responseBuffer.toString());

                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
                });
    }

}
