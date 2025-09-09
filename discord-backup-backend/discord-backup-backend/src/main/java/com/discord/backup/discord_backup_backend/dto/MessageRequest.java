package com.discord.backup.discord_backup_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MessageRequest {
    private String channelId;
    private String message;
}
