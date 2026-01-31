package garrcich.demo_temporal.workflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

public class StarterWorkflowWfTasksImpl implements StarterWorkflowWfTasks {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssnnnnnnnn");

    @Override
    public String moveFile(String filePath, String targetPath) {
        String movedFilePath = moveFileToTarget(filePath, targetPath);
        String checksum = calculateSha256(movedFilePath);
        return movedFilePath + " [SHA-256: " + checksum + "]";
    }

    private String moveFileToTarget(String filePath, String targetPath) {
        Path source = Paths.get(filePath);

        if (!Files.exists(source)) {
            throw new RuntimeException("Source file does not exist: " + filePath);
        }

        String fileName = source.getFileName().toString();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String newFileName = addTimestampToFileName(fileName, timestamp);

        Path targetDir = Paths.get(targetPath);
        Path destination = targetDir.resolve(newFileName);

        try {
            Files.createDirectories(targetDir);
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
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

    private String calculateSha256(String filePath) {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new RuntimeException("File does not exist: " + filePath);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(path);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hashBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
