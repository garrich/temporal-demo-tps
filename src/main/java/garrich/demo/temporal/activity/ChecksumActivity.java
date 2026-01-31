package garrich.demo.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ChecksumActivity {

    @ActivityMethod
    String calculateSha256(String filePath);
}
