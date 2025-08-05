#!/bin/sh

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
while ! nc -z mysql 3306; do
  sleep 1
done
echo "MySQL is ready!"

# Wait for Redis to be ready
echo "Waiting for Redis to be ready..."
while ! nc -z redis 6379; do
  sleep 1
done
echo "Redis is ready!"

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
while ! nc -z kafka 29092; do
  sleep 1
done
echo "Kafka is ready!"

# Start the application
echo "Starting the application..."
exec java -jar /app/app.jar