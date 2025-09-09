package com.discord.backup.discord_backup_backend.repository;

import com.discord.backup.discord_backup_backend.entity.UploadRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadRequestRepository extends JpaRepository<UploadRequest, Integer> {
    List<UploadRequest> findByStatus(String status);
}
