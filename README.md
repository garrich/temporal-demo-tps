[# Demo Temporal

A Spring Boot demonstration application showcasing file processing workflows using **Temporal Workflow Engine** combined with **Apache Camel** for file routing. This project demonstrates multiple approaches to implementing file operations through different workflow patterns.

## Technologies

| Technology   | Version | Purpose                                 |
|--------------|---------|-----------------------------------------|
| Spring Boot  | 4.0.2   |  Application framework                  |
| Java         | 21      | Programming language                    |
| Temporal SDK | 1.32.1  | Workflow orchestration engine           |
| Apache Camel | 4.10.0  | Integration framework with file routing |
| Lombok       | Latest  | Boilerplate code reduction              |

## Features

- **Three Workflow Patterns**: Demonstrates different approaches to implementing the same file operations
- **File Monitoring**: Automatic file detection using Apache Camel file component
- **SHA-256 Checksums**: Calculates file hashes for integrity verification
- **Timestamp Naming**: Prevents file conflicts with nanosecond-precision timestamps
- **Virtual Threads**: Leverages Java 21 virtual threads for lightweight concurrency
- **Autoscaling Pollers**: Dynamic adjustment of task pollers based on load

## Workflow Patterns

### 1. StarterWorkflow (Mixed Activity Pattern)
- Combines **Local Activities** (in-process) and **Remote Activities** (server-side)
- File movement as local activity, checksum calculation as remote activity with retry policy
- Source directory: `source-files`

### 2. Starter2Workflow (Local Activity Pattern)
- All operations run as **Local Activities** in-process
- Simpler, faster execution without network calls
- Source directory: `source-files2`

### 3. Starter3Workflow (Direct Execution Pattern)
- File operations implemented **directly in the workflow**
- No activity separation, suitable for simple synchronous operations
- Source directory: `source-files3`

## Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- Temporal Server running on localhost:7233

## Installation

```bash
# Clone the repository
git clone <repository-url>
cd demo-temporal

# Build the project
./mvnw clean install          # Linux/Mac
mvnw.cmd clean install        # Windows
```

## Running

### Start Temporal Server

Using Temporal CLI:
```bash
temporal server start-dev
```

Or using Docker:
```bash
docker run -p 7233:7233 temporalio/auto-setup:latest
```

### Start the Application

```bash
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows

# Or using built JAR
java -jar target/demo-temporal-0.0.1-SNAPSHOT.jar
```

## Usage

1. Place files in the source directories:
   - `D:/tmp/test-temporal/source` - processed by StarterWorkflow
   - `D:/tmp/test-temporal/source2` - processed by Starter2Workflow
   - `D:/tmp/test-temporal/source3` - processed by Starter3Workflow

2. Files are automatically moved to `D:/tmp/test-temporal/target` with timestamps appended

3. Check logs for workflow execution status and checksums

## Configuration

Configuration is managed in `application.yaml`:

```yaml
temporal:
  server-address: localhost:7233
  namespace: default
  task-queue: demo-task-queue
  source-path: D:/tmp/test-temporal/source
  source-path2: D:/tmp/test-temporal/source2
  source-path3: D:/tmp/test-temporal/source3
  target-path: D:/tmp/test-temporal/target
```

## Project Structure

```
src/main/java/garrcich/demo_temporal/
├── DemoTemporalApplication.java      # Main application
├── activity/
│   ├── FileActivity.java             # File operation interface
│   ├── FileActivityImpl.java         # File operation implementation
│   ├── ChecksumActivity.java         # Checksum interface
│   └── ChecksumActivityImpl.java     # Checksum implementation
├── workflow/
│   ├── StarterWorkflow.java          # Mixed activity workflow
│   ├── Starter2Workflow.java         # Local activity workflow
│   └── Starter3Workflow.java         # Direct execution workflow
├── route/
│   ├── FileProcessorRoute.java       # Camel route for StarterWorkflow
│   ├── FileProcessorRouteLocal.java  # Camel route for Starter2Workflow
│   └── FileProcessorRouteWf.java     # Camel route for Starter3Workflow
└── config/
    ├── TemporalConfig.java           # Temporal configuration beans
    └── TemporalProperties.java       # Configuration properties
```

## License

This project is for demonstration purposes.


## Performance Test Results

*Tested with 1000 files, each containing simple text content.*

| Route | Pattern | Files/sec | Avg Time/File | Total Time |
|-------|---------|-----------|---------------|------------|
| Route 1 | Mixed Activities (Local + Remote) | 4.02 | 248.87 ms | 4 min 9 sec |
| Route 2 | Local Activities Only | 20.10 | 49.76 ms | 50.45 sec |
| Route 3 | Direct Workflow Execution | 20.14 | 49.65 ms | 50.34 sec |
| Route 4 | Spring Service + SQLite | 66.21 | 15.10 ms | 15.78 sec |

### Conclusions

1. **Remote Activities are expensive**: Route 1 with remote activities is ~5x slower than local-only patterns due to network round-trips and Temporal server coordination overhead.

2. **Local Activities vs Direct Execution**: Routes 2 and 3 perform nearly identically (~20 files/sec), indicating that the Local Activity abstraction adds minimal overhead compared to direct workflow execution.

3. **Spring Service is fastest**: Route 4 without Temporal is ~3x faster than local Temporal patterns and ~16x faster than mixed activities. This is expected as it bypasses all workflow orchestration overhead.

4. **When to use each pattern**:
   - **Route 1 (Mixed)**: When you need retry policies, timeouts, and distributed execution for specific activities
   - **Route 2/3 (Local)**: When workflow history and replay capabilities are needed but all operations are local
   - **Route 4 (Spring)**: When maximum throughput is required and workflow orchestration features aren't needed 