package com.discord.backup.discord_backup_backend.repository;

import com.discord.backup.discord_backup_backend.entity.DownloadRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadRequestRepository extends JpaRepository<DownloadRequest, Integer> {
    List<DownloadRequest> findByStatus(String status);
}
