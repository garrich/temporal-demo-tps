package garrich.demo.temporal.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Component
public class ChecksumActivityImpl implements ChecksumActivity {

    @Override
    public String calculateSha256(String filePath) {
        var path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new RuntimeException("File does not exist: " + filePath);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(path);
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);
            String hash = HexFormat.of().formatHex(hashBytes);

            log.info("Calculated SHA-256 for {}: {}", filePath, hash);
            return hash;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
