
Для полного перезапуска проекта на сервере:

<code>chmod +x gradlew && docker-compose down && ./gradlew clean build -x test && ./gradlew prepareForDocker -x test && sleep 15 && docker-compose build --no- && sleep 15 && docker-compose up -d && docker-compose logs -f spacecore-bot</code>
