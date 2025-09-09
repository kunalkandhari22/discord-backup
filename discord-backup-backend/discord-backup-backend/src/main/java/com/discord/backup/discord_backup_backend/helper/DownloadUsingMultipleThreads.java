package com.discord.backup.discord_backup_backend.helper;

import com.discord.backup.discord_backup_backend.dto.DiscordMessageDto;
import com.discord.backup.discord_backup_backend.entity.UploadedFile;
import com.discord.backup.discord_backup_backend.service.DiscordService;
import discord4j.core.object.entity.Message;
import lombok.SneakyThrows;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DownloadUsingMultipleThreads extends Thread {

    int _minPartNumber;
    int _maxPartNumber;

    private final DiscordService _discordService;

    private final WebClient _webClient;

    String _name;

    List<UploadedFile> _messages;
    String _channelId;
    Path _downloadDir;

    public DownloadUsingMultipleThreads(String name, List<UploadedFile> messages, String channelId, Path downloadDir, String token, int minPartNumber, int maxPartNumber, WebClient webClient) {

        this._name = name;

        this._discordService = new DiscordService(token);

        this._webClient = webClient;

        this._minPartNumber = minPartNumber;
        this._maxPartNumber = maxPartNumber;

        this._messages = messages;
        this._channelId = channelId;
        this._downloadDir = downloadDir;
    }

    private volatile boolean shouldStop = false;

    public void stopGracefully() {
        this.shouldStop = true;
        this.interrupt();
    }

    @SneakyThrows
    @Override
    public void run() {
        int partNumber = 1;

        Message msg;

        for(UploadedFile file: _messages) {

            if (shouldStop) {
                System.out.println(_name + " received stop signal. Exiting...");
                return;
            }

            if(partNumber < _minPartNumber || partNumber > _maxPartNumber) {
                partNumber++;
                continue;
            }

            System.out.println(_name + " initiating " + partNumber + " at " + LocalDateTime.now());

            Message message = this._discordService.getMessageByMessageId(_channelId, file.getMessageId()).block();

            assert message != null;
            DiscordMessageDto messageDto = Helper.convertToDiscordMessageDto(message);

            String url = messageDto.getAttachments().getFirst().getUrl();
            String partFileName = messageDto.getAttachments().getFirst().getFilename();

            Path partFilePath = _downloadDir.resolve(partFileName);
            ;
            byte[] downloadedBytes = null;
            try {
                downloadedBytes = _webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .timeout(Duration.ofSeconds(60))
                        .block(Duration.ofSeconds(65));

                if (downloadedBytes != null) {
                    Files.write(partFilePath, downloadedBytes);
                    System.out.println("Successfully downloaded: " + partFileName + " to " + partFilePath.toAbsolutePath());
                } else {
                    throw new Exception("Downloaded bytes were null for " + partFileName + " from " + url);
                }

            } catch (Exception e) {
                throw new Exception("Failed to download or save " + partFileName + " from " + url + ": " + e.getMessage());
            }

            System.out.println(_name + " finished " + partNumber + " at " + LocalDateTime.now());
            partNumber++;
        }
    }
}
