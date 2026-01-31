package garrcich.demo_temporal.workflow;

import garrcich.demo_temporal.activity.ChecksumActivity;
import garrcich.demo_temporal.activity.FileActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class StarterWorkflowImpl implements StarterWorkflow {

    private final FileActivity fileActivity = Workflow.newActivityStub(
            FileActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    private final ChecksumActivity checksumActivity = Workflow.newActivityStub(
            ChecksumActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    @Override
    public String moveFile(String filePath, String targetPath) {
        String movedFilePath = fileActivity.moveFileToTarget(filePath, targetPath);
        String checksum = checksumActivity.calculateSha256(movedFilePath);
        return movedFilePath + " [SHA-256: " + checksum + "]";
    }
}
