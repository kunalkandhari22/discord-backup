package com.discord.backup.discord_backup_backend.controller;

import com.discord.backup.discord_backup_backend.dto.EditMessageRequest;
import com.discord.backup.discord_backup_backend.dto.MessageRequest;
import com.discord.backup.discord_backup_backend.dto.DiscordMessageDto;
import com.discord.backup.discord_backup_backend.entity.Channel;
import com.discord.backup.discord_backup_backend.exception.InternalServerErrorException;
import com.discord.backup.discord_backup_backend.helper.Helper;
import com.discord.backup.discord_backup_backend.service.ChannelService;
import com.discord.backup.discord_backup_backend.service.DiscordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discord")
public class DiscordController {

    @Autowired
    private DiscordService _service;

    @Autowired
    private ChannelService _channelService;

    //serverId - 1360678677087654228
    @PostMapping("/create-channel/{serverId}")
    public Mono<ResponseEntity<String>> createNewChannel(@PathVariable String serverId, @RequestParam String channelName) {
        return Mono.fromCallable(() -> {
                    try {
                        // The blocking call is now inside the callable, which will be executed
                        // on a thread provided by the Schedulers.boundedElastic() pool.
                        // This prevents the main reactor-http-nio thread from being blocked.
                        String createdChannel = this._service.createNewChannel(serverId, channelName).block();

                        Channel channel = new Channel(createdChannel, channelName, LocalDateTime.now(), LocalDateTime.now());

                        this._channelService.addChannel(channel);

                        return ResponseEntity.ok("Channel Created Successfully. Channel ID: " + createdChannel);
                    }
                    catch (Exception ex) {
                        // Throwing the exception here will be handled by the reactive stream,
                        // which will then propagate it to the onErrorResume block below.
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()) // This is crucial. It tells Reactor to run the blocking code on this scheduler.
                .onErrorResume(ex ->
                        // This is the reactive equivalent of a try-catch block.
                        Mono.just(ResponseEntity.status(500).body("Failed to create channel: " + ex.getMessage()))
                );
    }

    @DeleteMapping("/delete-channel/{channelId}")
    public Mono<ResponseEntity<String>> deleteChannel(@PathVariable String channelId) {
        return Mono.fromCallable(() -> {
                    try {
                        this._service.deleteChannel(channelId);

                        this._channelService.deleteChannel(channelId);

                        return ResponseEntity.ok("Channel Deleted Successfully");
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(500).body("Failed to delete channel: " + ex.getMessage()))
                );
    }

    @PostMapping("/send-message")
    public Mono<ResponseEntity<String>> sendMessage(@RequestBody MessageRequest messageRequest) {
        return Mono.fromCallable(() -> {
                    try {
                        Message sentMessage = this._service.sendMessage(messageRequest.getChannelId(), messageRequest.getMessage()).block();

                        return ResponseEntity.ok("Message Sent Successfully. Message ID: " + sentMessage.getId().asString());
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(500).body("Failed to send message: " + ex.getMessage()))
                );
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> uploadFile(@RequestParam String channelId, @RequestParam String message, @RequestPart("file") Mono<FilePart> filePartMono) {
        return filePartMono
                .flatMap(filePart -> {
                    return Mono.fromCallable(() -> {
                                try {
                                    Path tempFile = Files.createTempFile("upload_", "_" + filePart.filename());

                                    filePart.transferTo(tempFile).block();

                                    Message sentMessage = this._service.sendMessage(channelId, message, filePart.filename(), Files.newInputStream(tempFile)).block();

                                    Files.deleteIfExists(tempFile);

                                    return ResponseEntity.ok("Message Sent Successfully. Message ID: " + sentMessage.getId().asString());
                                }
                                catch (Exception ex) {
                                    throw new InternalServerErrorException(ex.getMessage() + "aa");
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(500).body("Failed to send message: " + ex.getMessage()))
                );
    }

    @DeleteMapping("/delete-message/{channelId}/{messageId}")
    public Mono<ResponseEntity<String>> deleteMessage(@PathVariable String channelId, @PathVariable String messageId) {
        return Mono.fromCallable(() -> {
                    try {
                        this._service.deleteMessage(channelId, Long.valueOf(messageId)).block();
                        return ResponseEntity.ok("Message Deleted Successfully.");
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(500).body("Failed to delete message: " + ex.getMessage()))
                );
    }

    @PutMapping("/edit-message")
    public Mono<ResponseEntity<String>> editMessage(@RequestBody EditMessageRequest newMessage) {
        return Mono.fromCallable(() -> {
                    try {
                        Message sentMessage = this._service.editMessage(newMessage.getChannelId(), newMessage.getMessageId(), newMessage.getNewContent()).block();

                        return ResponseEntity.ok("Message Edited Successfully. Message ID: " + sentMessage.getId().asString());
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(500).body("Failed to edit message: " + ex.getMessage()))
                );
    }

    @GetMapping("/get-message/{channelId}/{messageId}")
    public Mono<ResponseEntity<DiscordMessageDto>> getMessageById(@PathVariable String channelId, @PathVariable String messageId) {
        return Mono.fromCallable(() -> {
                    try {
                        Message message = this._service.getMessageByMessageId(channelId, messageId).block();

                        assert message != null;
                        DiscordMessageDto messageDto = Helper.convertToDiscordMessageDto(message);

                        return ResponseEntity.ok(messageDto);
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/get-message/{channelId}", params = "beforeMessageId")
    public Mono<ResponseEntity<List<DiscordMessageDto>>> getMessagesBeforeMessageId(@PathVariable Long channelId, @RequestParam Long beforeMessageId, @RequestParam Integer limit) {
        return Mono.fromCallable(() -> {
                    try {
                        List<Message> messages = this._service.getMessagesBeforeMessageId(channelId, beforeMessageId, limit).collectList().block();

                        List<DiscordMessageDto> messageDtos = messages.stream()
                                .map(Helper::convertToDiscordMessageDto)
                                .collect(Collectors.toList());

                        return ResponseEntity.ok(messageDtos);
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/get-message/{channelId}")
    public Mono<ResponseEntity<List<DiscordMessageDto>>> getMessagesByChannelId(@PathVariable String channelId, @RequestParam Integer limit) {
        return Mono.fromCallable(() -> {
                    try {
                        List<Message> messages = this._service.getMessagesByChannelId(channelId, limit).collectList().block();

                        List<DiscordMessageDto> messageDtos = messages.stream()
                                .map(Helper::convertToDiscordMessageDto)
                                .collect(Collectors.toList());

                        return ResponseEntity.ok(messageDtos);
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/delete-message/{channelId}")
    public Mono<ResponseEntity<String>> deleteMessagesByChannelId(@PathVariable String channelId) {
        return Mono.fromCallable(() -> {
                    try {
                        List<Message> messages = this._service.getMessagesByChannelId(channelId, 100).collectList().block();

                        for(int i = 0; i < Objects.requireNonNull(messages).size() ; i++) {
                            this._service.deleteMessage(channelId, messages.get(i).getId().asLong()).block();

                            if(i == messages.size() - 1) {
                                messages = this._service.getMessagesByChannelId(channelId, 100).collectList().block();
                                i=0;
                            }
                        }

                        return ResponseEntity.ok("Message Deleted Successfully.");
                    }
                    catch (Exception ex) {
                        throw new InternalServerErrorException(ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(500).body("Failed to delete message: " + ex.getMessage()))
                );
    }
}
