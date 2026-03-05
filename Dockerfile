# ── 1단계: 빌드 ──────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Gradle Wrapper 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 의존성 캐시 레이어
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# 소스 복사 후 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon --rerun-tasks

# ── 2단계: 실행 (경량 JRE) ────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
