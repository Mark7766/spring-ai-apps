mvn clean package -DskipTests
docker build -t text-to-sql:latest .
