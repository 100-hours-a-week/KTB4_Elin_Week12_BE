FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

ARG APP_UID=1000
ARG APP_GID=1000

RUN groupadd --gid "${APP_GID}" app \
    && useradd --uid "${APP_UID}" --gid "${APP_GID}" \
        --create-home --shell /usr/sbin/nologin app \
    && mkdir -p /app/data /app/uploads \
    && chown -R app:app /app

WORKDIR /app

COPY --from=builder --chown=app:app \
    /workspace/build/libs/*.jar \
    /app/app.jar

USER app

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx384m -XX:MaxMetaspaceSize=192m"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
