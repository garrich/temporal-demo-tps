package garrcich.demo_temporal.repository;

import garrcich.demo_temporal.entity.FileProcessingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileProcessingRepository extends JpaRepository<FileProcessingRecord, Long> {
}
