#!/bin/bash

# Check if the environment file exists
if [ -f ".docker.env" ]; then
  echo "Sourcing environment variables from .docker.env file"
  source .docker.env
else
  echo "No .docker.env file found"
fi

# Build the Gradle project and create a JAR file
echo "Building Gradle project"
./gradlew bootJar

# Start Docker Compose
echo "Starting Docker Compose"
docker compose -f compose-prod.yaml up -d
