# OaaS Image Resource: Network Digital Twin (NDT)

## Overview

The Network Digital Twin (NDT) is a microservice designed for integration with the OaaS platform, enabling advanced network simulations and Key Performance Indicator (KPI) evaluation. It supports containerized deployment using JIB for streamlined Docker image creation and registry uploads.

## Features

* __KPI Evaluation__: Analyze and evaluate critical network performance indicators.
* __OaaS Platform Compatibility__: Seamlessly integrates with the OaaS ecosystem.
* __Containerization Ready__: Built with JIB to easily generate and upload Docker images.
* __OpenAPI Support__: Access API documentation and testing via Swagger-UI.

## Running Locally

To run the NDT locally:

* Configure the Run Configurations in your IDE to use Spring Boot App.
* Start the application. This will launch the NDT microservice.
* Once running, navigate to: "/swagger-ui/index.html" to explore and interact with the OpenAPI documentation.

## Docker Registry
### Pushing a New Docker Image

To push a new Docker image to the registry:

1. Set up a Maven Build Configuration in your IDE:
        
- Base Directory: Path to your project workspace.
- Goals: clean package
- Skip Tests: Select True to skip tests during the build process.
- Parameters:
    * jib.to.auth.username: Your registry username (replace "user" with your actual username).
    * jib.to.auth.password: Your registry password (replace "password" with your actual password).

2. Run the Maven Build configuration to package the application and push the Docker image to the registry.

__Note__: Ensure you replace user and password with valid credentials for your Docker registry.