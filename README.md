# 태양연마 전산팀 OPEN AI ChatModel 판단형 챗봇

### 1. application.propertise 에 spring.ai.openai.api-key 키값 등록으로 OpenAI 관련 빈들 AutoConfig로 생성됨.
  - OpenAiChatModel
  - OpenAiImageModel
  - OepnAiAudioSpeechModel
  - OpenAiAudioTranscriptionModel

### 2. ChatMemory & JdbcChatMemoryRepository 멀티턴 구현, JPA ChatRepository로 전체 내용 저장

### 3. ChatClient
  - OpenAiChatModel을 Wrapper로 감싼 클래스로 아래 기능 사용이 가능
    - tools : LLM에 사용할 튤 붙임
    - advisor : RAG
    - entity : 응답 데이터 객체 파싱 (call method only)
    - 추상화 : 모델이 변경되어도 동일한 메소드