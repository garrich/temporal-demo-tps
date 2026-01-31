# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvnw.cmd clean install              # Windows
./mvnw clean install                # Linux/Mac

# Run application (requires Temporal server on localhost:7233)
mvnw.cmd spring-boot:run            # Windows
./mvnw spring-boot:run              # Linux/Mac

# Run tests
mvnw.cmd test                       # Windows
./mvnw test                         # Linux/Mac

# Run single test
mvnw.cmd test -Dtest=ClassName#methodName
```

## Starting Temporal Server

```bash
temporal server start-dev           # Using Temporal CLI
# OR
docker run -p 7233:7233 temporalio/auto-setup:latest
```

## Architecture

This is a Spring Boot 4 / Java 21 application demonstrating file processing using Temporal Workflow Engine with Apache Camel for file routing.

### Core Flow
1. **Camel Route** watches a source directory for files
2. When a file appears, the route invokes a **Temporal Workflow**
3. The workflow moves the file to target directory (adding timestamp) and calculates SHA-256 checksum

### Three Workflow Patterns

| Pattern | Route Class | Workflow Class | Source Dir | Description |
|---------|-------------|----------------|------------|-------------|
| Mixed Activities | `FileProcessorRoute` | `StarterWorkflowImpl` | `source-files` | Local + Remote activities with retry |
| Local Activities | `FileProcessorRouteLocal` | `StarterWorkflowLocalImpl` | `source-files2` | All operations in-process |
| Direct Execution | `FileProcessorRouteWf` | `StarterWorkflowWfTasksImpl` | `source-files3` | Logic directly in workflow |

### Key Components

- **`TemporalConfig`**: Configures WorkflowClient, WorkerFactory, and Worker; registers all workflow implementations and activities
- **`TemporalProperties`**: Configuration properties bound to `temporal.*` prefix in application.yaml
- **Activities**: `FileActivity` (file move) and `ChecksumActivity` (SHA-256 calculation) with implementations

### Configuration

Paths and Temporal settings are in `application.yaml` under `temporal.*` prefix. Default file paths point to `D:/tmp/test-temporal/`.
