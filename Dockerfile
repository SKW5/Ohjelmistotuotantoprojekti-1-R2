FROM maven:3.9-eclipse-temurin-21

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libasound2t64 \
        libfontconfig1 \
        libfreetype6 \
        libgl1 \
        libgtk-3-0t64 \
        libxi6 \
        libxrender1 \
        libxtst6 \
        libxxf86vm1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

CMD ["mvn", "-B", "javafx:run"]