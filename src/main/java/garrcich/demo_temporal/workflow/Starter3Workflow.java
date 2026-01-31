package garrcich.demo_temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Starter3Workflow {

    @WorkflowMethod
    String moveFile(String filePath, String targetPath);
}
