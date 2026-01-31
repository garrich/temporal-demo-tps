package garrcich.demo_temporal.workflow;

import garrcich.demo_temporal.activity.ChecksumActivity;
import garrcich.demo_temporal.activity.FileActivity;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class Starter2WorkflowImpl implements Starter2Workflow {

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
