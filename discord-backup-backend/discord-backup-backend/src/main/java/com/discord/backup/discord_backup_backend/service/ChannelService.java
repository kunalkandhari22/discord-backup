package com.discord.backup.discord_backup_backend.service;

import com.discord.backup.discord_backup_backend.entity.Channel;
import com.discord.backup.discord_backup_backend.repository.ChannelRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository _channelRepository;

    public ChannelService.PaginatedResult<Channel> getChannelsUsingPagination(int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, Sort.by("channelId").ascending());

        Page<Channel> pages =  this._channelRepository.findAll((Pageable) pageable);

        List<Channel> req = pages.getContent();

        return new ChannelService.PaginatedResult<Channel>(req, pages.getTotalElements());
    }

    public List<Channel> getAllChannels() {
        return this._channelRepository.findAll();
    }

    public Optional<Channel> getChannelById(String channelId) {
        return this._channelRepository.findByChannelId(channelId);
    }

    public void addChannel(Channel channel) {
        channel.setModifiedTs(LocalDateTime.now());
        channel.setCreationTs(LocalDateTime.now());

        this._channelRepository.save(channel);
    }

    @Transactional
    public void deleteChannel(String channelId) throws Exception {
        if(this._channelRepository.findByChannelId(channelId).orElse(null) == null)
            throw new Exception("Invalid Channel ID!!!");

        this._channelRepository.deleteByChannelId(channelId);
    }


    @Getter
    public static class PaginatedResult<T> {
        private final List<T> data;
        private final long total;

        public PaginatedResult(List<T> data, long total) {
            this.data = data;
            this.total = total;
        }
    }
}
