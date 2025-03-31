# Java 21 이미지 사용
FROM eclipse-temurin:21-jdk

# 작업 디렉토리 설정
WORKDIR /app

# jar 파일 복사 (프로젝트에서 build/libs 경로에 있어야 함)
COPY build/libs/*.jar app.jar

# 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]