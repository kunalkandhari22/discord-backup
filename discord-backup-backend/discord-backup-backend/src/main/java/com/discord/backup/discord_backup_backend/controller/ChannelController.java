package com.discord.backup.discord_backup_backend.controller;

import com.discord.backup.discord_backup_backend.entity.Channel;
import com.discord.backup.discord_backup_backend.exception.InternalServerErrorException;
import com.discord.backup.discord_backup_backend.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/")
public class ChannelController {
    @Autowired
    private ChannelService _channelService;

    @GetMapping("channel/allUsingPagination")
    public ResponseEntity<Map<String, Object>> getChannelsUsingPagination(@RequestParam int page, @RequestParam int pageSize) {
        try {
            ChannelService.PaginatedResult<Channel> result = this._channelService.getChannelsUsingPagination(page, pageSize);

            Map<String, Object> pages = new HashMap<>();
            pages.put("page", page);
            pages.put("pageSize", pageSize);
            pages.put("totalRecords", result.getTotal());
            pages.put("totalPages", (int) Math.ceil(result.getTotal() / (double) pageSize));

            Map<String, Object> response = new HashMap<>();
            response.put("pagination", pages);
            response.put("data", result.getData());

            return ResponseEntity.ok(response);
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to get list: " + ex.getMessage());
        }
    }

    @GetMapping("channel/all")
    public ResponseEntity<List<Channel>> getAllChannels() {
        try {
            return ResponseEntity.ok(this._channelService.getAllChannels());
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to get list: " + ex.getMessage());
        }
    }

    @GetMapping("channel/{channelId}")
    public ResponseEntity<Channel> getChannelById(@PathVariable String channelId) {
        try {
            Optional<Channel> result = this._channelService.getChannelById(channelId);

            if(result.isEmpty()) {
                throw new Exception("Invalid Channel ID!!!");
            }

            return ResponseEntity.ok(result.get());
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to get request data: " + ex.getMessage());
        }
    }

    //Add and Delete should only happen in DiscordController
//    @PostMapping("channel")
//    public ResponseEntity<String> addChannel(@RequestBody Channel channel) {
//        try {
//            this._channelService.addChannel(channel);
//
//            return ResponseEntity.ok("Data Added Successfully!!!");
//        }
//        catch (Exception ex) {
//            throw new InternalServerErrorException("Failed to add channel: " + ex.getMessage());
//        }
//    }
//
//    @DeleteMapping("channel/{channelId}")
//    public ResponseEntity<String> deleteUploadRequest(@PathVariable String channelId) {
//        try {
//            this._channelService.deleteChannel(channelId);
//
//            return ResponseEntity.ok("Data Deleted Successfully!!!");
//        }
//        catch (Exception ex) {
//            throw new InternalServerErrorException("Failed to delete channel: " + ex.getMessage());
//        }
//    }
}
