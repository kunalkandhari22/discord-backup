package com.discord.backup.discord_backup_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "upload_requests", schema = "config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="request_id")
    private Integer requestId;

    @Column(name = "input_path", length = 100)
    private String inputPath;

    @Column(name = "file_name", length = 50)
    private String fileName;

    @Column(name = "is_folder")
    private Boolean isFolder; // Using Boolean for 'bool' type

    @Column(name = "output_dir", length = 100)
    private String outputDir;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Column(name = "modified_ts")
    private LocalDateTime modifiedTs;
}
