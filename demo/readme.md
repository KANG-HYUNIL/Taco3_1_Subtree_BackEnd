# DiaryForMe 백엔드

사용자의 일상을 자동으로 기록해주는 모바일 앱 DiaryForMe의 백엔드 서버입니다.
사용자가 촬영한 사진과 위치 정보를 분석하여 AI를 통해 일기를 자동으로 생성합니다.

## 기술 스택
- Java 21
- Spring Boot
- Spring Data JPA
- Redis
- AWS S3
- OpenAI API

## 디렉토리 구조 (백엔드)

```
demo/
├── build.gradle                # Gradle 빌드 설정
├── settings.gradle             # Gradle 설정
├── Dockerfile                  # Docker 이미지 빌드 설정
├── gradlew                     # Gradle 래퍼 스크립트
├── gradlew.bat                 # Gradle 래퍼 스크립트 (Windows용)
├── gradle/                     # Gradle 래퍼 파일
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── logs/                       # 로그 파일 저장 디렉토리
│   └── application.log
├── src/
│   ├── main/
│   │   ├── java/com/taco1/demo/
│   │   │   │
│   │   │   ├── config/          # 설정 클래스
│   │   │   │   ├── AsyncConfig.java             # 비동기 처리 설정
│   │   │   │   ├── AwsCredentialsConfig.java    # AWS 자격 증명 설정
│   │   │   │   ├── OpenAIConfig.java            # OpenAI API 설정
│   │   │   │   ├── RedisConfig.java             # Redis 설정
│   │   │   │   └── WebClientConfig.java         # WebClient 설정
│   │   │   │
│   │   │   ├── controller/      # REST API 컨트롤러
│   │   │   │   ├── MainController.java          # 메인 API 엔드포인트
│   │   │   │   └── PushTokenController.java     # 푸시 알림 토큰 관리 컨트롤러
│   │   │   │
│   │   │   ├── converter/       # 데이터 변환기
│   │   │   │   └── PushTokenConverter.java      # 푸시 토큰 변환기 (유기)
│   │   │   │
│   │   │   ├── dto/             # 데이터 전송 객체
│   │   │   │   ├── DiaryDTO.java               # 일기 데이터 DTO
│   │   │   │   ├── MetadataDTO.java            # 메타데이터 DTO
│   │   │   │   ├── MetadataRequestDTO.java     # 메타데이터 요청 DTO
│   │   │   │   └── PushTokenDTO.java           # 푸시 토큰 DTO
│   │   │   │
│   │   │   ├── entity/          # JPA 엔티티
│   │   │   │   └── PushTokenEntity.java        # 푸시 토큰 엔티티 (유기)
│   │   │   │
│   │   │   ├── exception/       # 예외 처리
│   │   │   │   └── GlobalExceptionHandler.java # 전역 예외 핸들러
│   │   │   │
│   │   │   ├── message/         # 메시지 클래스
│   │   │   │   └── ExpoPushMessage.java        # Expo 푸시 메시지
│   │   │   │
│   │   │   ├── OpenAI/          # OpenAI 관련 클래스
│   │   │   │   ├── OpenAIProvider.java         # OpenAI 인터페이스
│   │   │   │   └── OpenAIProviderImpl.java     # OpenAI 구현체
│   │   │   │
│   │   │   ├── repository/      # 데이터 저장소
│   │   │   │   └── PushTokenRepository.java    # 푸시 토큰 레포지토리 (유기)
│   │   │   │
│   │   │   ├── service/         # 비즈니스 로직 서비스
│   │   │   │   ├── Blip3TaskProcessingService.java   # 이미지 처리 서비스
│   │   │   │   ├── FileService.java                  # 파일 관리 서비스 ( 유기)
│   │   │   │   ├── OpenAIAPIService.java             # OpenAI API 서비스
│   │   │   │   ├── PushTokenNotificationService.java # 푸시 알림 서비스
│   │   │   │   ├── RedisExpoTokenService.java        # Redis Expo 토큰 서비스
│   │   │   │   └── RedisService.java                 # Redis 서비스
│   │   │   │
│   │   │   └── DemoApplication.java    # 메인 애플리케이션 클래스
│   │   │
│   │   └── resources/
│   │       ├── application.yml          # 애플리케이션 설정
│   │       └── logback-spring.xml       # 로깅 설정
│   │
│   └── test/
│       └── java/com/taco1/demo/
│           ├── controller/
│           │   └── MainControllerTest.java    # 컨트롤러 테스트
│           ├── service/
│           │   ├── OpenAIAPIServiceTest.java        # OpenAI API 서비스 테스트
│           │   └── PushTokenNotificationServiceTest.java  # 푸시 알림 서비스 테스트
│           └── DemoApplicationTests.java      # 애플리케이션 테스트
└── build/                      # 빌드 결과물
    └── libs/                   # JAR 파일
        └── demo-0.0.1-SNAPSHOT.jar
```

## API 엔드포인트

### 1. Redis 조회 API
- **엔드포인트**: `POST /api/redis/check/{taskID}`
- **기능**: taskID를 이용해 외부 Redis에서 데이터 조회
- **요청 형식**: 
  ```
  POST /api/redis/check/{taskID}
  ```
  - Path Variable: taskID(String): 클라이언트에서 생성된 작업 고유 ID
- **응답 형식**: 
  - 성공 (200 OK): 
    ```json
    {
      "content": "일기 내용",
      "date": "2025-04-28",
      "topImageUri": "주요 이미지 URI",
      "imageUris": ["이미지1 URI", "이미지2 URI", ...]
    }
    ```
  - 실패 (404 Not Found): Redis에 해당 taskID의 데이터가 없을 경우
    ```json
    {
      "error": "Data not found for taskId: {taskID}"
    }
    ```

### 2. 메타데이터 처리 API
- **엔드포인트**: `POST /api/metadata`
- **기능**: 클라이언트에서 전송된 메타데이터를 처리하고 AI를 통해 일기 생성
- **요청 형식**: 
  ```
  POST /api/metadata
  Content-Type: multipart/form-data
  ```
  - FormData 필드:
    - `metadata`: JSON 문자열 형태의 메타데이터
      ```json
      {
        "images": [
          {
            "creationTime": "2025-04-28T15:30:00Z",
            "timeZone": "Asia/Seoul",
            "location": {
              "latitude": 37.5326,
              "longitude": 127.0246
            },
            "exif": {
              "DateTimeOriginal": "2025:04:28 15:30:00",
              "GPSLatitude": 37.5326,
              "GPSLongitude": 127.0246,
              "Make": "Samsung",
              "Model": "Galaxy S21"
            }
          },
          // ... 추가 이미지 데이터
        ],
        "task_id": "unique-task-id-12345",
        "token": "expo-push-notification-token"
      }
      ```
- **응답 형식**:
  - 성공 (200 OK): 메타데이터 검증 성공 시
    ```json
    {
      "message": "Metadata received successfully",
      "taskId": "unique-task-id-12345"
    }
    ```
  - 실패 (400 Bad Request): 메타데이터 형식 오류
    ```json
    {
      "error": "Invalid metadata format"
    }
    ```

### 3. 푸시 토큰 저장 API
- **엔드포인트**: `POST /token/save`
- **기능**: Expo 푸시 알림 토큰을 Redis에 저장
- **요청 형식**: 
  ```
  POST /token/save
  Content-Type: application/json
  ```
  - Body:
    ```json
    {
      "token": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]",
      "device": "Android" // 선택 사항
    }
    ```
- **응답 형식**:
  - 성공 (200 OK):
    ```json
    {
      "message": "Token saved successfully"
    }
    ```
  - 실패 (400 Bad Request): 토큰이 없거나 형식이 잘못된 경우
    ```json
    {
      "error": "Invalid token format"
    }
    ```

## DTO 클래스 구조

### 1. DiaryDTO
```java
public class DiaryDTO {
    private String content;       // 일기 내용
    private String date;          // 일기 날짜
    private String topImageUri;   // 대표 이미지 URI
    private List<String> imageUris; // 모든 이미지 URI 목록
}
```

### 2. MetadataDTO
```java
public class MetadataDTO {
    private String creationTime;      // 이미지 생성 시간 (ISO-8601 형식)
    private String timeZone;          // 시간대 정보 (예: "Asia/Seoul")
    private Location location;        // 위치 정보 객체
    private Map<String, Object> exif; // EXIF 메타데이터

    public static class Location {
        private Double latitude;      // 위도
        private Double longitude;     // 경도
    }

    // 유틸리티 메서드들
    public LocalDateTime getCreationTimeInClientZone() {...} // 클라이언트 시간대 기준 생성 시간
    public Double getExifLatitude() {...}                    // EXIF에서 위도 추출
    public Double getExifLongitude() {...}                   // EXIF에서 경도 추출
    public LocalDateTime getExifDateTimeOriginalAsDateTime() {...} // EXIF에서 날짜 추출
}
```

### 3. MetadataRequestDTO
```java
public class MetadataRequestDTO {
    private List<MetadataDTO> images;  // 이미지 메타데이터 목록
    private String task_id;           // 작업 식별자
    private String token;             // Expo 푸시 알림 토큰
}
```

### 4. PushTokenDTO
```java
public class PushTokenDTO {
    private String token;    // Expo 푸시 알림 토큰
    private String device;   // 디바이스 유형 (예: "Android", "iOS")
}
```

## OpenAI 관련 클래스 구조 및 역할

### 1. OpenAI 인터페이스 (OpenAIProvider)
```java
public interface OpenAIProvider {
    String generateResponse(String prompt);
}
```
- 역할: OpenAI API와의 인터랙션을 위한 인터페이스 정의
- 메서드:
  - `generateResponse(String prompt)`: 주어진 프롬프트에 대한 AI의 응답을 생성

### 2. OpenAI 구현체 (OpenAIProviderImpl)
```java
@Service
@Primary
public class OpenAIProviderImpl implements OpenAIProvider {
    private final OpenAIClient openAIClient;
    private final String systemMessage = "Hi, I am a helpful assistant...";
    private final Double temperature = 0.7;
    private final Integer maxTokens = 5000;
    
    public OpenAIProviderImpl(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }
    
    @Override
    public String generateResponse(String prompt) {
        // OpenAI API 호출 및 응답 처리 로직
    }
}
```
- 역할: OpenAI API를 활용하여 실제 AI 응답을 생성하는 구현체
- 주요 속성:
  - `openAIClient`: OpenAI API와 통신하기 위한 클라이언트
  - `systemMessage`: AI에게 전달되는 시스템 지시사항 (일기 형식 작성에 대한 안내)
  - `temperature`: 응답의 다양성을 조절하는 매개변수 (0.7: 균형잡힌 창의성)
  - `maxTokens`: 응답의 최대 토큰 수 (5000: 긴 일기를 생성하기에 충분한 길이)
- 메서드:
  - `generateResponse(String prompt)`: 프롬프트를 기반으로 GPT-4O 모델을 통해 일기를 생성

### 3. OpenAI API 서비스 (OpenAIAPIService)
```java
@Service
public class OpenAIAPIService {
    private final OpenAIProvider openAIProvider;
    
    public OpenAIAPIService(OpenAIProvider openAIProvider) {
        this.openAIProvider = openAIProvider;
    }
    
    public String generateChatResponse(String prompt) {
        return openAIProvider.generateResponse(prompt);
    }
}
```
- 역할: 컨트롤러와 OpenAI 구현체 간의 중간 레이어로, 서비스 로직 처리
- 메서드:
  - `generateChatResponse(String prompt)`: 컨트롤러로부터 요청을 받아 OpenAI Provider를 통해 응답 생성

## AI 연결 흐름

1. 클라이언트에서 메타데이터 전송 (`POST /api/metadata`)
2. `MainController`가 요청을 받아 메타데이터 유효성 검사 후 `Blip3TaskProcessingService`에 위임
3. `Blip3TaskProcessingService`에서 이미지 분석 데이터와 위치 정보를 조합하여 프롬프트 생성
4. `OpenAIAPIService`의 `generateChatResponse` 메서드 호출
5. `OpenAIProviderImpl`에서 OpenAI의 GPT-4O 모델 호출하여 일기 생성
6. 생성된 일기는 Redis에 저장되고, 푸시 알림을 통해 클라이언트에 완료 알림
7. 클라이언트는 task_id를 사용하여 생성된 일기를 조회 (`POST /api/redis/check/{taskID}`)

## AI 개발자를 위한 가이드

OpenAI 관련 코드를 수정하여 다른 AI 모델을 연동하거나 프롬프트를 조정하려면:

1. **프롬프트 수정**: `OpenAIProviderImpl`의 `systemMessage` 변수를 수정하여 일기 생성 스타일 변경
2. **모델 변경**: `OpenAIProviderImpl`의 `generateResponse` 메서드 내 `ChatModel.GPT_4O`를 다른 모델로 변경
3. **매개변수 조정**: `temperature`와 `maxTokens` 값을 조정하여 응답의 창의성과 길이 조절
4. **다른 AI 모델 연동**: `OpenAIProvider` 인터페이스를 구현하는 새로운 클래스 생성 후 Bean으로 등록