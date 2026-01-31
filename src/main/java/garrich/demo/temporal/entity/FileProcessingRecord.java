package garrich.demo.temporal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "file_processing_records")
public class FileProcessingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_path")
    private String originalPath;

    @Column(name = "new_path")
    private String newPath;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "operation")
    private String operation;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public FileProcessingRecord(String originalPath, String newPath, String checksum, String operation) {
        this.originalPath = originalPath;
        this.newPath = newPath;
        this.checksum = checksum;
        this.operation = operation;
        this.timestamp = LocalDateTime.now();
    }
}
