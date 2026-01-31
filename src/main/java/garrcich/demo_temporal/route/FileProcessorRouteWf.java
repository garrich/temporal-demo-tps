package garrcich.demo_temporal.route;

import garrcich.demo_temporal.config.TemporalProperties;
import garrcich.demo_temporal.workflow.Starter3Workflow;
import garrcich.demo_temporal.workflow.StarterWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessorRouteWf extends RouteBuilder {

    private final WorkflowClient workflowClient;
    private final TemporalProperties temporalProperties;

    @Override
    public void configure() {
        from("file:" + temporalProperties.getSourcePath3() + "?noop=false&delete=true")
                .routeId("file-processor-route-wf")
                .log("Processing file: ${header.CamelFileName}")
                .process(exchange -> {
                    String filePath = exchange.getIn().getHeader("CamelFileAbsolutePath", String.class);

                    WorkflowOptions options = WorkflowOptions.newBuilder()
                            .setTaskQueue(temporalProperties.getTaskQueue())
                            .setWorkflowId("starter-" + UUID.randomUUID())
                            .build();

                    Starter3Workflow workflow = workflowClient.newWorkflowStub(Starter3Workflow.class, options);
                    String result = workflow.moveFile(filePath, temporalProperties.getTargetPath());

                    exchange.getIn().setBody(result);
                })
                .log("Workflow completed. File moved to: ${body}");
    }
}
