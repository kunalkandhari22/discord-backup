package com.discord.backup.discord_backup_backend.helper;

import com.discord.backup.discord_backup_backend.entity.UploadedFile;
import com.discord.backup.discord_backup_backend.repository.UploadedFileRepository;
import com.discord.backup.discord_backup_backend.service.DiscordService;
import discord4j.core.object.entity.Message;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UploadUsingMultipleThreads extends Thread {

    int requestId;
    Path uploadDir;
    String channelId;
    String fileName;

    List<Path> partFiles;

    int _minPartNumber;
    int _maxPartNumber;

    private final UploadedFileRepository _uploadFileRepository;

    private final DiscordService _discordService;

    String name;

    public UploadUsingMultipleThreads(String name, int requestId, Path uploadDir, String channelId, String fileName, List<Path> partFiles, String token, UploadedFileRepository uploadFileRepository, int minPartNumber, int maxPartNumber) {

        this.name = name;
        this.requestId = requestId;
        this.channelId = channelId;
        this.uploadDir = uploadDir;
        this.fileName = fileName;

        this.partFiles = partFiles;

        this._discordService = new DiscordService(token);

        this._uploadFileRepository = uploadFileRepository;
        this._minPartNumber = minPartNumber;
        this._maxPartNumber = maxPartNumber;
    }

    private volatile boolean shouldStop = false;

    public void stopGracefully() {
        this.shouldStop = true;
        this.interrupt();
    }

    @SneakyThrows
    @Override
    public void run() {

        UploadedFile file;

        int partNumber = 1;

        Message msg;

        Optional<Long> maxPartNumber = this._uploadFileRepository.findMaxPartNumberByRequestId(requestId, _minPartNumber, _maxPartNumber);

        for(Path partFile: partFiles) {

            if (shouldStop) {
                System.out.println(name + " received stop signal. Exiting...");
                return;
            }

            if(partNumber < _minPartNumber || partNumber > _maxPartNumber) {
                partNumber++;
                continue;
            }

            if(maxPartNumber.isPresent() && partNumber <= maxPartNumber.get()) {
                partNumber++;
                continue;
            }
            System.out.println(name + " initiating " + partNumber + " at " + LocalDateTime.now());

            try (InputStream inputStream = Files.newInputStream(partFile)) {
                msg = this._discordService.sendMessage(channelId, Integer.toString(partNumber), partFile.getFileName().toString(), inputStream).block();
            }

            assert msg != null;
            if((msg.getAttachments().getFirst().getSize() < 9000000 && partNumber != partFiles.size()))    {
                this._discordService.deleteMessage(channelId, msg.getId().asLong());

                throw new Exception("Received invalid size.");
            }

            file = new UploadedFile(requestId, partNumber, msg.getId().asString(), LocalDateTime.now(), LocalDateTime.now());

            this._uploadFileRepository.save(file);

            System.out.println(name + " finished " + partNumber + " at " + LocalDateTime.now());
            partNumber++;
        }
    }
}
