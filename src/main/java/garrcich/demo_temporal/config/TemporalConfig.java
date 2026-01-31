package garrcich.demo_temporal.config;

import garrcich.demo_temporal.activity.ChecksumActivity;
import garrcich.demo_temporal.activity.FileActivity;
import garrcich.demo_temporal.workflow.Starter2WorkflowImpl;
import garrcich.demo_temporal.workflow.Starter3WorkflowImpl;
import garrcich.demo_temporal.workflow.StarterWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.tuning.PollerBehaviorAutoscaling;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TemporalConfig {

    private final TemporalProperties temporalProperties;
    private final FileActivity fileActivity;
    private final ChecksumActivity checksumActivity;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        var builder = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalProperties.getServerAddress())
                .setRpcTimeout(temporalProperties.getServer().getRpcTimeout())
                .setConnectionBackoffResetFrequency(temporalProperties.getServer().getConnectionBackoffResetFrequency())
                .setRpcLongPollTimeout(temporalProperties.getServer().getRpcTimeout().multipliedBy(3));

        if (temporalProperties.getServer().isEnableHttps()) {
            builder.setEnableHttps(true);
        }

        return WorkflowServiceStubs.newServiceStubs(builder.build());
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
        return WorkflowClient.newInstance(
                workflowServiceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(temporalProperties.getNamespace())
                        .build()
        );
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        var factoryOptions = WorkerFactoryOptions.newBuilder()
                .setMaxWorkflowThreadCount(temporalProperties.getWorker().getMaxConcurrentWorkflowTaskExecutionSize())
                .build();

        return WorkerFactory.newInstance(workflowClient, factoryOptions);
    }

    @Bean
    public Worker worker(WorkerFactory workerFactory) {
        var workerProps = temporalProperties.getWorker();

        var workerOptions = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskExecutionSize(workerProps.getMaxConcurrentWorkflowTaskExecutionSize())
                .setMaxConcurrentActivityExecutionSize(workerProps.getMaxConcurrentActivityExecutionSize())
                .setMaxConcurrentLocalActivityExecutionSize(workerProps.getMaxConcurrentLocalActivityExecutionSize())
                .setUsingVirtualThreads(true)
                .setActivityTaskPollersBehavior(
                        new PollerBehaviorAutoscaling(workerProps.getMinPollers(), workerProps.getMaxPollers(), workerProps.getInitialPollers())
                )
                .setWorkflowTaskPollersBehavior(
                        new PollerBehaviorAutoscaling(workerProps.getMinPollers(), workerProps.getMaxPollers(), workerProps.getInitialPollers())
                )
                .setDefaultDeadlockDetectionTimeout(workerProps.getDeadlockDetectionTimeout())
                .build();

        var worker = workerFactory.newWorker(temporalProperties.getTaskQueue(), workerOptions);
        worker.registerWorkflowImplementationTypes(StarterWorkflowImpl.class, Starter2WorkflowImpl.class, Starter3WorkflowImpl.class);
        worker.registerActivitiesImplementations(fileActivity, checksumActivity);
        return worker;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWorkerFactory(ApplicationReadyEvent event) {
        var workerFactory = event.getApplicationContext().getBean(WorkerFactory.class);
        workerFactory.start();
        log.info("Temporal worker started on task queue: {}", temporalProperties.getTaskQueue());
    }
}
