# Use an openjdk base image
FROM  --platform=linux/amd64 openjdk:23-slim

# Create a user to run the application as (instead of root)
# RUN addgroup -S app && adduser -S app -G app
RUN groupadd --system app && useradd --system --gid app --no-create-home --shell /usr/sbin/nologin app

# Switch to the "app" user
USER app

# Define the working directory
WORKDIR /app

# Copy the jar file into the Docker image
COPY build/libs/*.jar /app/app.jar

EXPOSE 10000

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]