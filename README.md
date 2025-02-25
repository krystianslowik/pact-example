
# Pact Producer & Configuration Guide

This document provides a comprehensive guide for setting up and running the Pact Producer—a Kotlin Spring Boot service with a REST interface—and explains the minimal Pact configuration in the `pom.xml`. It also includes an overview of the simple Node.js Pact Consumer used in this setup. This guide is intended for developers who are already familiar with Pact, Spring Boot, Maven, and related technologies.

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Maven Setup & Dependencies](#maven-setup--dependencies)
    - [Pact-Related Properties](#pact-related-properties)
    - [Pact Dependencies](#pact-dependencies)
    - [Pact Maven Plugin Configuration](#pact-maven-plugin-configuration)
    - [Additional Build Configurations](#additional-build-configurations)
- [Using Pact in the Project](#using-pact-in-the-project)
    - [Provider Side](#provider-side)
    - [Consumer Side](#consumer-side)
- [Running the Project & Tests](#running-the-project--tests)
- [Additional Notes](#additional-notes)

---

## Overview

This project is a Pact Producer built with Kotlin and Spring Boot. It exposes a REST API to serve project details using JPA for persistence. Provider tests are implemented with the Pact JVM framework using JUnit 5. The consumer is a simple Node.js application that generates and publishes Pact files to a Pact Broker (self-hosted at `http://jrse.firewall-gateway.com:9292`).

---

## Prerequisites

- **Java 17** (or later)
- **Maven**
- **Kotlin**
- An operational **Pact Broker** (self-hosted or via [pactflow.io](https://pactflow.io))

---

## Project Structure

Below is an overview of the project directory layout:

~~~
.
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pact-comsumer-example
│   ├── package-lock.json
│   ├── package.json
│   ├── pacts
│   │   └── pact-consumer-example-pact-example.json
│   └── tests
│       └── consumer.test.js
├── pom.xml
└── src
    ├── main
    │   ├── kotlin
    │   │   └── com
    │   │       └── example
    │   │           └── pact
    │   │               ├── PactApplication.kt
    │   │               ├── ProjectRepository.kt
    │   │               └── RESTController.kt
    │   └── resources
    │       ├── application.properties
    │       ├── static
    │       └── templates
    └── test
        └── kotlin
            └── com
                └── example
                    ├── PactApplicationTests.kt
                    └── RESTControllerTest.kt
~~~

---

## Maven Setup & Dependencies

This section explains the minimal Pact-related configuration within the `pom.xml`.

### Pact-Related Properties

The `<properties>` section defines key properties:

- **pact-jvm.version**:  
  Sets the version for all Pact JVM dependencies (e.g., `4.6.8`).

- **pactBrokerUrl**:  
  Holds the URL of your Pact Broker (`http://jrse.firewall-gateway.com:9292`).

Example:

~~~
<properties>
  <java.version>17</java.version>
  <kotlin.version>1.9.25</kotlin.version>
  <pact-jvm.version>4.6.8</pact-jvm.version>
  <pactBrokerUrl>http://jrse.firewall-gateway.com:9292</pactBrokerUrl>
</properties>
~~~

### Pact Dependencies

The following dependencies are essential for running Pact provider tests:

- **au.com.dius.pact.provider:junit5spring**:  
  Integrates Pact with JUnit 5 and Spring Boot testing. Enables tests using annotations such as `@Provider` and `@State`.

- **au.com.dius.pact.provider:junit5**:  
  Provides core Pact functionality for verifying that the provider meets the consumer contract.

These dependencies are scoped as `test`.

Example:

~~~
<dependency>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>junit5spring</artifactId>
  <version>${pact-jvm.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>junit5</artifactId>
  <version>${pact-jvm.version}</version>
  <scope>test</scope>
</dependency>
~~~

### Pact Maven Plugin Configuration

The Pact Maven Plugin is configured in the `<build>` section under `<plugins>`. Key elements include:

- **Plugin Coordinates**:  
  Identified by `au.com.dius.pact.provider:maven` with the version `${pact-jvm.version}`.

- **Configuration Block**:  
  Specifies the `<serviceProviders>` element with one or more `<serviceProvider>` entries.

- **Mandatory Elements in `<serviceProvider>`**:
    - `<name>`: Provider name (must match the consumer contract), e.g., `pact-example`.
    - `<protocol>`: Communication protocol, typically `http`.
    - `<host>`: Host address where the provider runs, e.g., `localhost`.
    - `<port>`: Port number (e.g., `8080`).
    - `<path>`: Base path, set as `/`.
    - `<pactBroker>`: URL of the Pact Broker, using `${pactBrokerUrl}`.

Example:

~~~
<plugin>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>maven</artifactId>
  <version>${pact-jvm.version}</version>
  <configuration>
    <serviceProviders>
      <serviceProvider>
        <name>pact-example</name>
        <protocol>http</protocol>
        <host>localhost</host>
        <port>8080</port>
        <path>/</path>
        <pactBroker>${pactBrokerUrl}</pactBroker>
      </serviceProvider>
    </serviceProviders>
  </configuration>
</plugin>
~~~

### Additional Build Configurations

Other plugins in the `pom.xml` ensure the project builds and tests run correctly:

- **Spring Boot Maven Plugin**:  
  Packages the Spring Boot application.

- **Kotlin Maven Plugin**:  
  Configured with `-Xjsr305=strict` and the `spring` compiler plugin for proper Kotlin compilation.

- **Maven Surefire Plugin**:  
  Configured with `<useSystemClassLoader>false</useSystemClassLoader>` to address JUnit 5 class loading issues.

---

## Using Pact in the Project

### Consumer Side
The consumer is a simple Node.js application. Below is the `package.json`:
~~~
{
  "name": "pact-consumer-example",
  "version": "1.0.0",
  "scripts": {
    "test": "mocha \"tests/**/*.js\"",
    "publish-pact": "npx pact-broker publish pacts --consumer-app-version 1.0.0 --broker-base-url http://jrse.firewall-gateway.com:9292"
  },
  "dependencies": {
    "axios": "^0.27.2"
  },
  "devDependencies": {
    "@pact-foundation/pact": "^11.0.0",
    "mocha": "^10.0.0",
    "chai": "^4.3.6"
  }
}
~~~

And an excerpt from the consumer test (`consumer.test.js`):

~~~
const path = require('path');
const { Pact, Matchers } = require('@pact-foundation/pact');
const axios = require('axios');
const { like } = Matchers;
const { expect } = require('chai');

describe('Pact with pact-example', () => {
    const provider = new Pact({
        consumer: 'pact-consumer-example',
        provider: 'pact-example',
        port: 1234,
        log: path.resolve(process.cwd(), 'logs', 'pact.log'),
        dir: path.resolve(process.cwd(), 'pacts'),
        logLevel: 'INFO'
    });

    before(async () => {
        await provider.setup();
    });

    after(async () => {
        await provider.finalize();
    });

    afterEach(async () => {
        await provider.verify();
    });

    describe('GET /projects/123', () => {
        before(async () => {
            await provider.addInteraction({
                state: 'project with ID 123 exists',
                uponReceiving: 'a request to get project 123',
                withRequest: {
                    method: 'GET',
                    path: '/projects/123'
                },
                willRespondWith: {
                    status: 200,
                    headers: { 'Content-Type': 'application/json' },
                    body: {
                        id: like('123'),
                        name: like('Agile Transformation'),
                        status: like('Active')
                    }
                }
            });
        });

        it('returns the project details', async () => {
            const response = await axios.get('http://localhost:1234/projects/123');
            expect(response.status).to.equal(200);
            expect(response.data).to.deep.equal({
                id: '123',
                name: 'Agile Transformation',
                status: 'Active'
            });
        });
    });
});
~~~

### Provider Side

- The producer exposes a REST endpoint via `RESTController.kt` and uses `ProjectRepository.kt` for persistence.
- Provider tests (in `RESTControllerTest.kt`) use Pact annotations to define interaction states and verify responses.
    - For example, the state `"project with ID 123 exists"` is used to set up the repository with the expected data.

Example of setting up a provider state:

~~~
@State("project with ID 123 exists")
fun setupProject123() {
    projectRepository.save(Project(id = "123", name = "Agile Transformation", status = "Active"))
}
~~~
---

## Running the Project & Tests

### Provider Tests

To run the provider tests and publish verification results, execute:

~~~
mvn clean install -Dpact.verifier.publishResults=true
~~~

This command runs all tests and, with the system property set, ensures that verification results are published to the Pact Broker.

### Consumer Tests

In the `pact-consumer-example` directory, install dependencies and run tests with:

~~~
npm install
npm run test
npm run publish-pact
~~~

This will generate the Pact files and publish them to the Pact Broker at the configured URL.

---

## Additional Notes

- **Pact Broker**:  
  This guide references a self-hosted Pact Broker (`http://jrse.firewall-gateway.com:9292`). Adjust this URL if you are using a different broker service.

- **Port Configuration**:  
  Ensure that the provider port (`8080`) and the consumer mock provider port (`1234`) do not conflict with other services.

- **CI/CD Integration**:  
  While this guide focuses on local setup, additional steps for integrating these tests into a CI/CD pipeline can be added later.