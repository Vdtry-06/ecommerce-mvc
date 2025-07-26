# Defining variables
APP_NAME=ecommerce
DOCKER_COMPOSE=docker-compose.yml

# Build the application
build:
	mvn clean package -DskipTests

# Build Docker image
docker-build:
	docker build -t $(APP_NAME):latest .

# Run Docker Compose
up:
	docker-compose -f $(DOCKER_COMPOSE) up -d

# Stop Docker Compose
down:
	docker-compose -f $(DOCKER_COMPOSE) down

# Clean up Docker resources
clean:
	docker-compose -f $(DOCKER_COMPOSE) down -v
	docker rmi $(APP_NAME):latest

# Run tests
test:
	mvn test

# Full build and deploy
deploy: build docker-build up

.PHONY: build docker-build up down clean test deploy