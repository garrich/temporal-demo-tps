package garrich.demo.temporal.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FileActivityImpl implements FileActivity {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssnnnnnnnn");

    @Override
    public String moveFileToTarget(String filePath, String targetPath) {
        var source = Paths.get(filePath);

        if (!Files.exists(source)) {
            throw new RuntimeException("Source file does not exist: " + filePath);
        }

        var fileName = source.getFileName().toString();
        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        var newFileName = addTimestampToFileName(fileName, timestamp);

        var targetDir = Paths.get(targetPath);
        var destination = targetDir.resolve(newFileName);

        try {
            Files.createDirectories(targetDir);
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Moved file from {} to {}", source, destination);
            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file: " + e.getMessage(), e);
        }
    }

    private String addTimestampToFileName(String fileName, String timestamp) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String name = fileName.substring(0, dotIndex);
            String extension = fileName.substring(dotIndex);
            return name + "_" + timestamp + extension;
        }
        return fileName + "_" + timestamp;
    }
}
