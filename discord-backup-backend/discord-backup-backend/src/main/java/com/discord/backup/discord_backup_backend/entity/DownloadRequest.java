package com.discord.backup.discord_backup_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_requests", schema = "config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DownloadRequest {
    @Id
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "download_dir", length = 100)
    private String downloadDir;

    @Column(name = "file_name", length = 50)
    private String fileName;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Column(name = "modified_ts")
    private LocalDateTime modifiedTs;
}
