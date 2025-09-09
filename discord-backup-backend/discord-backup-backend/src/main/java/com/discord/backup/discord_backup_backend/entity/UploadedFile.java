package com.discord.backup.discord_backup_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files", schema = "config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(UploadedFileId.class)
public class UploadedFile {
    @Id
    @Column(name = "request_id")
    private Integer requestId;
    @Id
    @Column(name = "part_number")
    private Integer partNumber;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Column(name = "modified_ts")
    private LocalDateTime modifiedTs;
}
