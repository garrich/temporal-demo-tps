package garrcich.demo_temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface FileActivity {

    @ActivityMethod
    String moveFileToTarget(String filePath, String targetPath);
}
