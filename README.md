# 태양연마 전산팀 OPEN AI ChatModel 판단형 챗봇

### 1. application.propertise 에 spring.ai.openai.api-key 키값 등록으로 OpenAI 관련 빈들 AutoConfig로 생성됨.
  - OpenAiChatModel
  - OpenAiImageModel
  - OepnAiAudioSpeechModel
  - OpenAiAudioTranscriptionModel

### 2. ChatMemory & JdbcChatMemoryRepository 멀티턴 구현, JPA ChatRepository로 전체 내용 저장
  - 전체 대화내용 저장을 위한 테이블

    
    CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    `conversation_id` VARCHAR(36) NOT NULL,
    `content` TEXT NOT NULL,
    `type` ENUM('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') NOT NULL,
    `timestamp` TIMESTAMP NOT NULL,

    INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`, `timestamp`));
    
### 3. ChatClient
  - OpenAiChatModel을 Wrapper로 감싼 클래스로 아래 기능 사용이 가능
    - tools : LLM에 사용할 튤 붙임
    - advisor : RAG
    - entity : 응답 데이터 객체 파싱 (call method only)
    - 추상화 : 모델이 변경되어도 동일한 메소드

### 4. Structured Output
  - LLM 응답을 구조화하여 자바 객체로 받기
    - 원리 : 프롬프트 보낼때 포맷대로 응답을 요청 -> 포맷에 맞게 파싱
    - CityResponseDTO의 속성으로 city 라는 변수명의 문자열 리스트 타입을 만듦.
    - ChatClient를 통해 call메소드로 호출하고 entity메소드에 구조화 하려는 DTO를 인자로 넘겨 구조화된 응답값을 받음.

### 5. Agent를 위한 Tool Calling
  - Agent란 LLM이 스스로 판단하여 행동하는 방식
  - LLM이 문제 해결을 위해 외부 도움이 필요하다고 판단하면 Tool 사용해 작업 수행
    - 툴 샘플 (검색 튤, DB조회 튤, 계산 튤, 코드 실행 튤, 시각화 튤)
  - 실행순서
    1. 사용자의 프로프트와 튤 목록을 담아 LLM API 호출
    2. LLM API가 사용자의 질문과 튤 목록을 확인 후, 특정 튤을 사용하겠다고 콜백함.
    3. 튤을 활용하여 데이터를 처리하고 
    4. 튤 실행 결과를 다시 LLM API에 전달
    5. LLM API의 최종 응답

### 6. RAG를 위한 Advisor
  - RAG란 LLM에게 우리 도메인의 지식을 부여하기 위해 프롬프트에 N개의 데이터를 더해서 보내는 기법
  - 구현 : ChatClient advisors 메소드는 RAG를 통합할 수 있는 기능 제공, advisors메소드에 VectorStore객체를 넣어주어야함.
~~~
implementation 'org.springframework.ai:spring-ai-advisors-vector-store'
~~~
  - 벡터 DB는 Elasticsearch
~~~
implementation 'org.springframework.ai:spring-ai-starter-vector-store-elasticsearch'
~~~
  - VectorStoreConfig 설정으로 연결 뿐만 아니라, 스프링 기반의 자동 인덱스 생성, 자연어 추가시 자동 임베딩 API 연결을 처리할 수 있습니다.