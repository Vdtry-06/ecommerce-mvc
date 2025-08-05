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

# View logs
logs:
	docker-compose -f $(DOCKER_COMPOSE) logs -f

# View specific service logs
logs-app:
	docker-compose -f $(DOCKER_COMPOSE) logs -f app

logs-kafka:
	docker-compose -f $(DOCKER_COMPOSE) logs -f kafka

logs-mysql:
	docker-compose -f $(DOCKER_COMPOSE) logs -f mysql

# Clean up Docker resources
clean:
	docker-compose -f $(DOCKER_COMPOSE) down -v
	docker rmi $(APP_NAME):latest || true
	docker system prune -f

# Run tests
test:
	mvn test

# Full build and deploy
deploy: build docker-build up

# Restart services
restart:
	docker-compose -f $(DOCKER_COMPOSE) restart

# Check service status
status:
	docker-compose -f $(DOCKER_COMPOSE) ps

# Create .env file from example
env:
	cp .env.example .env
	@echo "Please edit .env file with your actual values"

.PHONY: build docker-build up down logs logs-app logs-kafka logs-mysql clean test deploy restart status env