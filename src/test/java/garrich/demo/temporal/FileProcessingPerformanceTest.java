package garrich.demo.temporal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Manual performance test for file processing routes.
 * This test is excluded from normal builds. Run manually with:
 *   mvnw.cmd test -Dtest=FileProcessingPerformanceTest#testRoute4Performance
 *   mvnw.cmd test -Dtest=FileProcessingPerformanceTest#testRoutePerformance
 * Prerequisites:
 *   1. Temporal server running: temporal server start-dev
 *   2. Application running: mvnw.cmd spring-boot:run
 *   3. Source and target directories must exist
 */
//@Disabled("Manual performance test - run explicitly with -Dtest=FileProcessingPerformanceTest")
@Tag("manual")
class FileProcessingPerformanceTest {

    private static final String BASE_PATH = "D:/tmp/test-temporal";
    private static final String TARGET_PATH = BASE_PATH + "/target";

    private static final int DEFAULT_FILE_COUNT = 1000;
    private static final int POLL_INTERVAL_MS = 100;
    private static final int TIMEOUT_SECONDS = 300;

    /**
     * Test Route 4 (Spring Service + SQLite) with default 1000 files.
     */
    @Test
    void testRoute4Performance() throws Exception {
        runPerformanceTest(BASE_PATH + "/source4", DEFAULT_FILE_COUNT);
    }

    /**
     * Test Route 1 (Mixed Activities) with default 1000 files.
     */
    @Test
    void testRoute1Performance() throws Exception {
        runPerformanceTest(BASE_PATH + "/source", DEFAULT_FILE_COUNT);
    }

    /**
     * Test Route 2 (Local Activities) with default 1000 files.
     */
    @Test
    void testRoute2Performance() throws Exception {
        runPerformanceTest(BASE_PATH + "/source2", DEFAULT_FILE_COUNT);
    }

    /**
     * Test Route 3 (Direct Workflow Execution) with default 1000 files.
     */
    @Test
    void testRoute3Performance() throws Exception {
        runPerformanceTest(BASE_PATH + "/source3", DEFAULT_FILE_COUNT);
    }

    /**
     * Parameterized test with different file counts.
     * Run with: mvnw.cmd test -Dtest="FileProcessingPerformanceTest#testRoutePerformance"
     */
    @ParameterizedTest
    @ValueSource(ints = {100, 500, 1000})
    void testRoutePerformance(int fileCount) throws Exception {
        runPerformanceTest(BASE_PATH + "/source4", fileCount);
    }

    private void runPerformanceTest(String sourcePath, int fileCount) throws Exception {
        Path sourceDir = Paths.get(sourcePath);
        Path targetDir = Paths.get(TARGET_PATH);

        System.out.println("=".repeat(60));
        System.out.println("PERFORMANCE TEST");
        System.out.println("=".repeat(60));
        System.out.println("Source directory: " + sourcePath);
        System.out.println("Target directory: " + TARGET_PATH);
        System.out.println("File count: " + fileCount);
        System.out.println("=".repeat(60));

        // Verify directories exist
        if (!Files.exists(sourceDir)) {
            throw new IllegalStateException("Source directory does not exist: " + sourcePath);
        }
        if (!Files.exists(targetDir)) {
            throw new IllegalStateException("Target directory does not exist: " + TARGET_PATH);
        }

        // Clean source directory
        cleanDirectory(sourceDir);

        // Count existing files in target (to know how many new ones to expect)
        long initialTargetCount = countFiles(targetDir);
        System.out.println("Initial files in target: " + initialTargetCount);

        // Generate files in a temp staging directory first
        Path stagingDir = Files.createTempDirectory("perf-test-staging");
        System.out.println("Generating " + fileCount + " files in staging area...");

        for (int i = 1; i <= fileCount; i++) {
            String filename = "file" + i + ".txt";
            String content = "content" + i;
            Files.writeString(stagingDir.resolve(filename), content);
        }
        System.out.println("Files generated.");

        // Move all files to source directory and start timer
        System.out.println("Moving files to source directory...");
        Instant startTime = Instant.now();

        try (Stream<Path> files = Files.list(stagingDir)) {
            files.forEach(file -> {
                try {
                    Files.move(file, sourceDir.resolve(file.getFileName()));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to move file: " + file, e);
                }
            });
        }

        Instant filesMovedTime = Instant.now();
        System.out.println("All files moved to source directory.");
        System.out.println("Time to move files: " + Duration.between(startTime, filesMovedTime).toMillis() + " ms");

        // Wait for all files to appear in target directory
        System.out.println("Waiting for files to be processed...");
        long expectedTargetCount = initialTargetCount + fileCount;

        Instant deadline = Instant.now().plusSeconds(TIMEOUT_SECONDS);
        long currentCount;
        int lastReportedProgress = 0;

        while ((currentCount = countFiles(targetDir)) < expectedTargetCount) {
            if (Instant.now().isAfter(deadline)) {
                throw new RuntimeException("Timeout waiting for files. Expected: " + expectedTargetCount +
                        ", Found: " + currentCount);
            }

            // Report progress every 10%
            int progress = (int) (((currentCount - initialTargetCount) * 100) / fileCount);
            if (progress >= lastReportedProgress + 10) {
                System.out.println("Progress: " + (currentCount - initialTargetCount) + "/" + fileCount +
                        " (" + progress + "%)");
                lastReportedProgress = progress;
            }

            TimeUnit.MILLISECONDS.sleep(POLL_INTERVAL_MS);
        }

        Instant endTime = Instant.now();

        // Calculate results
        Duration totalDuration = Duration.between(startTime, endTime);
        Duration processingDuration = Duration.between(filesMovedTime, endTime);
        double totalSeconds = totalDuration.toMillis() / 1000.0;
        double processingSeconds = processingDuration.toMillis() / 1000.0;
        double filesPerSecondTotal = fileCount / totalSeconds;
        double filesPerSecondProcessing = fileCount / processingSeconds;

        // Print results
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("RESULTS");
        System.out.println("=".repeat(60));
        System.out.println("Files processed:          " + fileCount);
        System.out.println("Total time:               " + formatDuration(totalDuration));
        System.out.println("Processing time:          " + formatDuration(processingDuration));
        System.out.println("Files/second (total):     " + String.format("%.2f", filesPerSecondTotal));
        System.out.println("Files/second (processing):" + String.format("%.2f", filesPerSecondProcessing));
        System.out.println("Avg time per file:        " + String.format("%.2f", processingSeconds * 1000 / fileCount) + " ms");
        System.out.println("=".repeat(60));

        // Cleanup staging directory
        Files.deleteIfExists(stagingDir);
    }

    private void cleanDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.err.println("Warning: Could not delete " + file);
                    }
                });
            }
        }
    }

    private long countFiles(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return 0;
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(Files::isRegularFile).count();
        }
    }

    private String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + " ms";
        } else if (millis < 60000) {
            return String.format("%.2f seconds", millis / 1000.0);
        } else {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            return String.format("%d min %d sec", minutes, seconds);
        }
    }
}
