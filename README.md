# Developing microservices with Kotlin

This repository contains the source code used for the blog post `Developing Microservices in Kotlin: Part-I to Part-V`.
There are two microservices:

- **shop-user-service**: This service handles shop user data
- **shop-item-service**: This service handles shop item data
- **shop-deployment**: Contains the deployment script - docker compose

## Run the application

Build the jar for each service and then build its image with tag. Refer to deployment and services readme files.

## Command
```
Setting up gradle wrapper makes the project executable in any IDE even without gradle
/usr/libexec/java_home -V

export JAVA_HOME=$(/usr/libexec/java_home -v 21.0.7)
export PATH=$JAVA_HOME/bin:$PATH

./gradlew wrapper --gradle-version 9.1.0
```
