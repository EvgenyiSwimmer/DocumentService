# DocumentService
Это сервис для работы с документами:
1. документы создаются
2. переводятся по статусам
3. по каждому изменению ведётся история
4. при утверждении создаётся запись в реестре
5. есть фоновая обработка
6. есть утилита массового создания документов

## Проект состоит из двух модулей
- document-service - основной сервис для работы с документами
- document-generator - утилита для массового создания документов

### Что нужно, чтобы запустить
1.  Java 17 - 21
2.  Maven
3.  Docker Desktop
4. PgAdmin для удобного управления базой данных (опционально)
5. Postman или curl для тестирования API
#### Запускаем PostgreSQL через Docker
- Открой терминал или Git Bash
- Перейди в корень проекта
- Выполните: docker compose up -d
После этого поднимется PostgreSQL. Если всё успешно, в Docker Desktop будет контейнер postgres.
- или ручками создаем БД - documents в postgreSQL и подключаемся в idea через Database tool window

##### Запуск backend-сервиса
Класс: document-service
Нажми ▶ Run (mvn -pl document-service spring-boot:run)
Если всё запустилось успешно:
Tomcat started on port 8080
Started DocumentServiceApplication
Сервис будет доступен по адресу: http://localhost:8080

###### Запуск generator-сервиса
mvn -pl document-generator spring-boot:run
Generator config: document-generator/src/main/resources/application.yml

###### Проверка работы
Создать документ
в Postman или curl:
POST http://localhost:8080/documents
Content-Type: application/json

Тело:
{
"author": "test",
"title": "My document"
}

Ожидаемый ответ:
{
"id": 1,
"number": "DOC-...",
"author": "test",
"title": "My document",
"status": "DRAFT",
...
}

###### Как проверить прогресс по логам
В логах можно увидеть:

При создании документов:
Created 1 of 100
Created 2 of 100
...

При работе SUBMIT-worker: SUBMIT-worker: batchSize=50, picked=50, success=50, durationMs=45
При работе APPROVE-worker: APPROVE-worker: batchSize=50, picked=50, success=49, registryErr=1, durationMs=60

Это означает:
1. сколько документов обработано
2. сколько успешно
3. сколько ошибок
4. сколько заняло времени

*****************
Для 5000+ id: батчирование + set-based операции в БД + идемпотентность

Для выноса реестра:
отдельный сервис + своя БД
доставка через outbox → Kafka → consumer
с уникальными ключами в реестре, чтобы событие можно было обработать повторно без дублей.
