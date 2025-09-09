package com.discord.backup.discord_backup_backend.repository;

import com.discord.backup.discord_backup_backend.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Integer> {
    Optional<Channel> findByChannelId(String channelId);
    void deleteByChannelId(String channelId);
}
