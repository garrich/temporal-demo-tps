package garrcich.demo_temporal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "temporal")
public class TemporalProperties {

    private String serverAddress = "localhost:7233";
    private String namespace = "default";
    private String taskQueue = "default-task-queue";
    private String sourcePath = "source-files";
    private String sourcePath2 = "source-files2";
    private String sourcePath3 = "source-files3";
    private String targetPath = "target-files";

    private Server server = new Server();
    private Worker worker = new Worker();

    @Data
    public static class Server {
        private Duration rpcTimeout = Duration.ofSeconds(10);
        private Duration connectionBackoffResetFrequency = Duration.ofSeconds(10);
        private Duration grpcReconnectFrequency = Duration.ofSeconds(10);
        private boolean enableHttps = false;
    }

    @Data
    public static class Worker {
        private int maxConcurrentWorkflowTaskExecutionSize = 200;
        private int maxConcurrentActivityExecutionSize = 200;
        private int maxConcurrentLocalActivityExecutionSize = 200;
        private int minPollers = 2;
        private int maxPollers = 100;
        private int initialPollers = 5;
        private long deadlockDetectionTimeout = Duration.ofSeconds(60).toMillis();
    }
}
