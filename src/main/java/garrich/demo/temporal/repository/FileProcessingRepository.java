package garrich.demo.temporal.repository;

import garrich.demo.temporal.entity.FileProcessingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileProcessingRepository extends JpaRepository<FileProcessingRecord, Long> {
}
