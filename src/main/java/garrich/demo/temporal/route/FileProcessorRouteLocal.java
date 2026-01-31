package garrich.demo.temporal.route;

import garrich.demo.temporal.config.TemporalProperties;
import garrich.demo.temporal.workflow.StarterWorkflowLocal;
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
public class FileProcessorRouteLocal extends RouteBuilder {

    private final WorkflowClient workflowClient;
    private final TemporalProperties temporalProperties;

    @Override
    public void configure() {
        from("file:" + temporalProperties.getSourcePath2() + "?noop=false&delete=true")
                .routeId("file-processor-route-local")
                .log("Processing file: ${header.CamelFileName}")
                .process(exchange -> {
                    String filePath = exchange.getIn().getHeader("CamelFileAbsolutePath", String.class);

                    var options = WorkflowOptions.newBuilder()
                            .setTaskQueue(temporalProperties.getTaskQueue())
                            .setWorkflowId("starter-" + UUID.randomUUID())
                            .build();

                    var workflow = workflowClient.newWorkflowStub(StarterWorkflowLocal.class, options);
                    var result = workflow.moveFile(filePath, temporalProperties.getTargetPath());

                    exchange.getIn().setBody(result);
                })
                .log("Workflow completed. File moved to: ${body}");
    }
}
