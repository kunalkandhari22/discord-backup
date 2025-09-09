package com.discord.backup.discord_backup_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadedFileId implements Serializable {

    private Integer requestId;
    private Integer partNumber;

}