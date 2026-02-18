# ITQ Documents

## Run Postgres
docker compose up -d

## Run service
mvn -pl document-service spring-boot:run

## Run generator
mvn -pl document-generator spring-boot:run

Generator config: document-generator/src/main/resources/application.yml

## Logs
- generator logs show N and progress
- workers logs show batch processing and duration
- API returns per-id results for submit/approve

Для 5000+ id:
батчирование + set-based операции в БД + идемпотентность

Для выноса реестра:
отдельный сервис + своя БД
доставка через outbox → Kafka → consumer
с уникальными ключами в реестре, чтобы событие можно было обработать повторно без дублей.
