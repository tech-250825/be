# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:jdk21-graal-jammy AS builder

# 환경 변수 설정 (TLS 강제 설정)
ENV JAVA_TOOL_OPTIONS="-Dhttps.protocols=TLSv1.2,TLSv1.3"

# 작업 디렉토리 설정
WORKDIR /app

# Ubuntu 기반 패키지 설치 (SSL 문제 방지)
RUN apt-get update && apt-get install -y ca-certificates curl wget unzip && update-ca-certificates

# Gradle 배포 파일을 컨테이너 내부로 복사
COPY gradle-8.6-bin.zip /root/.gradle/wrapper/dists/gradle-8.6-bin/gradle-8.6-bin.zip

# 소스 코드 및 Gradle 래퍼 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 래퍼 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle 종속성 다운로드 (캐시 활용)
# RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

# Gradle 종속성 다운로드 (인터넷에서 받지 않고, 미리 복사된 ZIP 사용)
RUN ./gradlew dependencies --no-daemon --offline

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon --offline

# 애플리케이션 빌드
# RUN --mount=type=cache,target=/root/.gradle ./gradlew build --no-daemon

# 두 번째 스테이지: 실행 스테이지
FROM ghcr.io/graalvm/jdk-community:21

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행할 JAR 파일 지정
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
