package garrcich.demo_temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface StarterWorkflowLocal {

    @WorkflowMethod
    String moveFile(String filePath, String targetPath);
}
