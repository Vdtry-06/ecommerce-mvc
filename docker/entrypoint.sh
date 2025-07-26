#!/bin/sh
set -e

# Wait for MySQL to be ready
until mysqladmin ping -h mysql -u root -proot --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

# Wait for Kafka to be ready
until kafka-topics.sh --bootstrap-server kafka:9092 --list; do
    echo "Waiting for Kafka to be ready..."
    sleep 2
done

# Wait for Redis to be ready
until redis-cli -h redis ping; do
    echo "Waiting for Redis to be ready..."
    sleep 2
done

# Start the Java application
exec java -jar app.jar