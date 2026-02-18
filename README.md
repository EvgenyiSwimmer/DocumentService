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
