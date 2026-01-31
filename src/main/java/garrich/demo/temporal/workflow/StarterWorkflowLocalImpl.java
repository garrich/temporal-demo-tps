package garrich.demo.temporal.workflow;

import garrich.demo.temporal.activity.ChecksumActivity;
import garrich.demo.temporal.activity.FileActivity;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class StarterWorkflowLocalImpl implements StarterWorkflowLocal {

    private final FileActivity fileActivity = Workflow.newLocalActivityStub(
            FileActivity.class,
            LocalActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
                    .build()
    );

    private final ChecksumActivity checksumActivity = Workflow.newLocalActivityStub(
            ChecksumActivity.class,
            LocalActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build()
    );

    @Override
    public String moveFile(String filePath, String targetPath) {
        String movedFilePath = fileActivity.moveFileToTarget(filePath, targetPath);
        String checksum = checksumActivity.calculateSha256(movedFilePath);
        return movedFilePath + " [SHA-256: " + checksum + "]";
    }
}
