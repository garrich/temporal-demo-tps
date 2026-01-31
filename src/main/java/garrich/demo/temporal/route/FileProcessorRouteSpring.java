package garrich.demo.temporal.route;

import garrich.demo.temporal.config.TemporalProperties;
import garrich.demo.temporal.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessorRouteSpring extends RouteBuilder {

    private final FileProcessingService fileProcessingService;
    private final TemporalProperties temporalProperties;

    @Override
    public void configure() {
        from("file:" + temporalProperties.getSourcePath4() + "?noop=false&delete=true")
                .routeId("file-processor-route-spring")
                .log("Processing file: ${header.CamelFileName}")
                .process(exchange -> {
                    String filePath = exchange.getIn().getHeader("CamelFileAbsolutePath", String.class);
                    String result = fileProcessingService.processFile(filePath, temporalProperties.getTargetPath());
                    exchange.getIn().setBody(result);
                })
                .log("Processing completed. File moved to: ${body}");
    }
}
