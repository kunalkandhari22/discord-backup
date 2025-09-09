package com.discord.backup.discord_backup_backend.repository;

import com.discord.backup.discord_backup_backend.entity.UploadedFile;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Integer> {
    @Transactional
    void deleteByRequestId(Integer requestId);

    List<UploadedFile> findByRequestIdOrderByPartNumberAsc(Integer requestId);

    Page<UploadedFile> findByRequestId(Integer requestId, Pageable pageable);

    @Query(value = "SELECT MAX(part_number) FROM config.uploaded_files uf where uf.request_id=:requestId", nativeQuery = true)
    Optional<Long> findMaxPartNumberByRequestId(@Param("requestId") Integer requestId);

    @Query(value = "SELECT MAX(part_number) FROM config.uploaded_files uf where uf.request_id=:requestId AND uf.part_number BETWEEN :minPartNumber AND :maxPartNumber", nativeQuery = true)
    Optional<Long> findMaxPartNumberByRequestId(@Param("requestId") Integer requestId, int minPartNumber, int maxPartNumber);
}
