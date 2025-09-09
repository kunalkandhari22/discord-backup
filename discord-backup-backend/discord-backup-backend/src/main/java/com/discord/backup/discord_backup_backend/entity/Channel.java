package com.discord.backup.discord_backup_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "channels", schema = "config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Channel {
    @Id
    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "channel_name", length = 50)
    private String channelName;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Column(name = "modified_ts")
    private LocalDateTime modifiedTs;
}
