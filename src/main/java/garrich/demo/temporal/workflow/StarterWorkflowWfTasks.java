package garrich.demo.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface StarterWorkflowWfTasks {

    @WorkflowMethod
    String moveFile(String filePath, String targetPath);
}
