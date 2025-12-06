# SecureCoda - Activity & Exposure Monitor

This is a implementation of **SecureCoda**, a monitoring and remediation system for Coda documents.

## Overview

Features implemented:
- Connects to Coda API using WebClient.
- Poller that periodically lists documents, tables, rows and scans for sensitive information.
- Detection rules for credit-card-like numbers, SSN, emails, and password keywords.
- Alerts persisted in an mysql database.

## How to run

Requirements: Java 17, Maven or Docker.


## Configuration

Set `coda.api.token` in `src/main/resources/application.properties` or via environment variable `CODA_API_TOKEN` to use real Coda API calls. 

## Architecture

- Spring Boot WebFlux for HTTP interactions with Coda.
- Scheduled `PollerService` triggers detection periodically.
- `DetectionService` includes regex-based rules; easily extensible.
- Alerts stored via Spring Data JPA.
- Frontend served from `src/main/resources/static`.

