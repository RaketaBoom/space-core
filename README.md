
Для полного перезапуска проекта на сервере:

<code>chmod +x gradlew && docker-compose down && ./gradlew clean build -x test && ./gradlew prepareForDocker && docker-compose build --no-cache && docker-compose up -d && docker-compose logs -f spacecore-bot</code>
