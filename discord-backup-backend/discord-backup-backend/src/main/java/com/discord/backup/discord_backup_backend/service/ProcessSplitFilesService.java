package com.discord.backup.discord_backup_backend.service;

import com.discord.backup.discord_backup_backend.dto.DiscordMessageDto;
import com.discord.backup.discord_backup_backend.entity.UploadedFile;
import com.discord.backup.discord_backup_backend.helper.DownloadUsingMultipleThreads;
import com.discord.backup.discord_backup_backend.helper.Helper;
import com.discord.backup.discord_backup_backend.helper.UploadUsingMultipleThreads;
import com.discord.backup.discord_backup_backend.repository.UploadedFileRepository;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class ProcessSplitFilesService {
    private static final Pattern PART_FILE_PATTERN = Pattern.compile("(.+)\\.part(\\d{3})$");

    @Autowired
    private DiscordService _discordService;

    @Autowired
    private final WebClient webClient;

    @Autowired
    private UploadedFileRepository _uploadFileRepository;

    private final Environment _environment;

    public ProcessSplitFilesService(WebClient webClient, Environment environment) {
        this.webClient = webClient;

        this._environment = environment;
    }

    public void uploadFilesToChannel(int requestId, Path uploadDir, String channelId, String fileName) throws Exception {
        if (!Files.exists(uploadDir) || !Files.isDirectory(uploadDir)) {
            throw new Exception("Invalid Upload Directory.");
        }

        List<Path> partFiles;

        try (Stream<Path> files = Files.list(uploadDir)) {
            partFiles = files
                    .filter(p -> p.getFileName().toString().startsWith(fileName + ".part"))
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingInt(this::partNumber))
                    .toList();
        }

        if (partFiles.isEmpty())
            throw new Exception("Invalid upload directory(No file found).");

//        Mono<Void> mono = startUploadProcess(partFiles, channelId, requestId);
//
//        mono.subscribe();

        InputStream inputStream;

        UploadedFile file;

        int partNumber = 1;

        Optional<Long> maxPartNumber = this._uploadFileRepository.findMaxPartNumberByRequestId(requestId);

        for (Path partFile : partFiles) {

            if (maxPartNumber.isPresent() && partNumber <= maxPartNumber.get()) {
                partNumber++;
                continue;
            }

            inputStream = Files.newInputStream(partFile);

            Message msg = this._discordService.sendMessage(channelId, Integer.toString(partNumber), partFile.getFileName().toString(), inputStream).block();

            assert msg != null;
            if ((msg.getAttachments().getFirst().getSize() < 9000000 && partNumber != partFiles.size())) {
                this._discordService.deleteMessage(channelId, msg.getId().asLong());

                throw new Exception("Received invalid size.");
            }

            file = new UploadedFile(requestId, partNumber, msg.getId().asString(), LocalDateTime.now(), LocalDateTime.now());

            this._uploadFileRepository.save(file);

            partNumber++;
        }
    }

    public void uploadFilesToChannelMulThread(int requestId, Path uploadDir, String channelId, String fileName) throws Exception {
        if (!Files.exists(uploadDir) || !Files.isDirectory(uploadDir)) {
            throw new Exception("Invalid Upload Directory.");
        }

        List<Path> partFiles;

        try (Stream<Path> files = Files.list(uploadDir)) {
            partFiles = files
                    .filter(p -> p.getFileName().toString().startsWith(fileName + ".part"))
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingInt(this::partNumber))
                    .toList();
        }

        if (partFiles.isEmpty())
            throw new Exception("Invalid upload directory(No file found).");

        int fileToProcess = partFiles.size() / 4;

        String token = this._environment.getProperty("discord.bot.token");
        String token2 = this._environment.getProperty("discord.bot.token2");
        String token3 = this._environment.getProperty("discord.bot.token3");
        String token4 = this._environment.getProperty("discord.bot.token4");

        UploadUsingMultipleThreads up1 = new UploadUsingMultipleThreads("Thread 1", requestId, uploadDir, channelId, fileName, partFiles, token, this._uploadFileRepository, 1, fileToProcess);
        UploadUsingMultipleThreads up2 = new UploadUsingMultipleThreads("Thread 2", requestId, uploadDir, channelId, fileName, partFiles, token2, this._uploadFileRepository, 1 + fileToProcess, fileToProcess * 2);
        UploadUsingMultipleThreads up3 = new UploadUsingMultipleThreads("Thread 3", requestId, uploadDir, channelId, fileName, partFiles, token3, this._uploadFileRepository, 1 + fileToProcess * 2, fileToProcess * 3);
        UploadUsingMultipleThreads up4 = new UploadUsingMultipleThreads("Thread 4", requestId, uploadDir, channelId, fileName, partFiles, token4, this._uploadFileRepository, 1 + fileToProcess * 3, fileToProcess * 4 + partFiles.size() % 4);

        int[] minExpectedForThreads = new int[]{1, 1 + fileToProcess, 1 + fileToProcess * 2, 1 + fileToProcess * 3};
        int[] maxExpectedForThreads = new int[]{fileToProcess, fileToProcess * 2, fileToProcess * 3, fileToProcess * 4 + partFiles.size() % 4};

        up1.start();
        up2.start();
        up3.start();
        up4.start();

        try {
            while(up1.getState() != Thread.State.TERMINATED || up2.getState() != Thread.State.TERMINATED || up3.getState() != Thread.State.TERMINATED || up4.getState() != Thread.State.TERMINATED) {
                System.out.println(up1.getState());
                System.out.println(up2.getState());
                System.out.println(up3.getState());
                System.out.println(up4.getState());

                if(up1.getState() == Thread.State.TERMINATED) {
                    Optional<Long> maxUploaded = this._uploadFileRepository.findMaxPartNumberByRequestId(requestId, minExpectedForThreads[0], maxExpectedForThreads[0]);

                    if(maxUploaded.isEmpty()) {
                        throw new Exception("Unable to validate uploaded files!!!");
                    }

                    if(maxExpectedForThreads[0] != maxUploaded.get()) {
                        throw new Exception("All files are not uploaded!!!");
                    }
                }

                if(up2.getState() == Thread.State.TERMINATED) {
                    Optional<Long> maxUploaded = this._uploadFileRepository.findMaxPartNumberByRequestId(requestId, minExpectedForThreads[1], maxExpectedForThreads[1]);

                    if(maxUploaded.isEmpty()) {
                        throw new Exception("Unable to validate uploaded files!!!");
                    }

                    if(maxExpectedForThreads[1] != maxUploaded.get()) {
                        throw new Exception("All files are not uploaded!!!");
                    }
                }

                if(up3.getState() == Thread.State.TERMINATED) {
                    Optional<Long> maxUploaded = this._uploadFileRepository.findMaxPartNumberByRequestId(requestId, minExpectedForThreads[2], maxExpectedForThreads[2]);

                    if(maxUploaded.isEmpty()) {
                        throw new Exception("Unable to validate uploaded files!!!");
                    }

                    if(maxExpectedForThreads[2] != maxUploaded.get()) {
                        throw new Exception("All files are not uploaded!!!");
                    }
                }

                if(up4.getState() == Thread.State.TERMINATED) {
                    Optional<Long> maxUploaded = this._uploadFileRepository.findMaxPartNumberByRequestId(requestId, minExpectedForThreads[3], maxExpectedForThreads[3]);

                    if(maxUploaded.isEmpty()) {
                        throw new Exception("Unable to validate uploaded files!!!");
                    }

                    if(maxExpectedForThreads[3] != maxUploaded.get()) {
                        throw new Exception("All files are not uploaded!!!");
                    }
                }

                Thread.sleep(3000);
            }
        } catch (Exception e) {
            System.err.println("Exception caught! Stopping the other thread.");

            if (up1.isAlive()) {
                up1.stopGracefully();
            }
            if (up2.isAlive()) {
                up2.stopGracefully();
            }
            if (up3.isAlive()) {
                up3.stopGracefully();
            }
            if (up4.isAlive()) {
                up4.stopGracefully();
            }

            throw e;
        }
    }

    private int partNumber(Path partFile) {
        Matcher matcher = PART_FILE_PATTERN.matcher(partFile.getFileName().toString());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(2));
        }
        return Integer.MAX_VALUE;
    }

//     public Mono<Void> startUploadProcess(List<Path> filesToUpload, Long channelId, Integer requestId) {
//             int desiredConcurrency = 10; // Adjust this based on your needs and Discord's rate limits
//             return uploadFilesConcurrently(filesToUpload, channelId, requestId, desiredConcurrency)
//                 .doOnSuccess(v -> System.out.println("All files uploaded successfully!"))
//                 .doOnError(e -> System.err.println("Overall upload process failed: " + e.getMessage()));
//     }

    /**
     * Uploads multiple files to a Discord channel concurrently.
     *
     * @param partFiles   A list of Path objects, each representing a file part to upload.
     * @param channelId   The ID of the Discord channel to upload to.
     * @param requestId   The ID of the original request.
     * @param concurrency The maximum number of files to upload simultaneously.
     * @return A Mono<Void> that completes when all uploads are done, or an error if any fails.
     */
//    public Mono<Void> uploadFilesConcurrently(List<Path> partFiles, Long channelId, Integer requestId, int concurrency) {
//        // Use Flux.fromIterable to create a reactive stream from your list of file paths.
//        // Then, use flatMap to process each file concurrently.
//        return Flux.fromIterable(partFiles)
//                // .zipWith(Flux.range(1, partFiles.size()), (path, index) -> new Tuple2<>(path, index)) // If you need a sequential partNumber
//                // OR if partNumber is already implied by list order:
//                .flatMap(partFile -> {
//                    int partNumber = partFiles.indexOf(partFile) + 1; // Simple way to get 1-based index as partNumber
//
//                    // Blocking I/O (Files.newInputStream) should be offloaded to a dedicated scheduler.
//                    // Schedulers.boundedElastic() is good for blocking tasks.
//                    return Mono.usingWhen(
//                                    // Resource supplier: Create InputStream from file path
//                                    Mono.fromCallable(() -> Files.newInputStream(partFile))
//                                            .subscribeOn(Schedulers.boundedElastic()), // Offload file system access
//
//                                    // Mono factory: Use the InputStream to send the message
//                                    inputStream -> {
//                                        String filename = partFile.getFileName().toString();
//                                        String messageContent = Integer.toString(partNumber); // Or whatever content you want
//
//                                        // Call DiscordService's sendMessage, which returns a Mono<Message>
//                                        return this._discordService.sendMessage(channelId, messageContent, filename, inputStream)
//                                                .flatMap(msg -> {
//                                                    // Save UploadedFile metadata to repository (this might be blocking JPA)
//                                                    return Mono.fromCallable(() -> {
//                                                                UploadedFile file = new UploadedFile(requestId, partNumber, msg.getId().asLong(), LocalDateTime.now(), LocalDateTime.now());
//                                                                return this._uploadFileRepository.save(file); // This is blocking, so offload
//                                                            })
//                                                            .subscribeOn(Schedulers.boundedElastic()) // Offload repository save
//                                                            .thenReturn(msg); // Continue the flow with the Message object
//                                                });
//                                    },
//                                    // Resource cleanup: Close the InputStream
//                                    inputStream -> Mono.fromRunnable(() -> {
//                                        try {
//                                            inputStream.close();
//                                        } catch (IOException e) {
//                                            System.err.println("Error closing input stream for part file " + partFile.getFileName() + ": " + e.getMessage());
//                                        }
//                                    }).subscribeOn(Schedulers.boundedElastic()) // Offload close operation
//                            )
//                            // Add error handling for each individual file upload
//                            .onErrorResume(e -> {
//                                System.err.println("Failed to upload or save metadata for part " + partFile.getFileName() + ": " + e.getMessage());
//                                return Mono.empty(); // Skip this file if it fails, allowing others to continue
//                            });
//                }, concurrency) // <--- THIS IS THE CONCURRENCY CONTROL!
//                .then(); // Convert the Flux<Message> to Mono<Void> to signal overall completion
//    }
    public void downloadFilesFromChannel(int requestId, Path downloadDir, String fileName, String channelId) throws Exception {

        downloadDir = Path.of(downloadDir.toString(), fileName);

        if (Files.isDirectory(downloadDir)) {
            try (Stream<Path> walk = Files.walk(downloadDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        Files.createDirectories(downloadDir);

        List<UploadedFile> messages = this._uploadFileRepository.findByRequestIdOrderByPartNumberAsc(requestId);

        for (UploadedFile file : messages) {
            Message message = this._discordService.getMessageByMessageId(channelId, file.getMessageId()).block();

            DiscordMessageDto messageDto = Helper.convertToDiscordMessageDto(message);

            String url = messageDto.getAttachments().getFirst().getUrl();
            String partFileName = messageDto.getAttachments().getFirst().getFilename();

            Path partFilePath = downloadDir.resolve(partFileName);
            ;
            byte[] downloadedBytes = null;
            try {
                downloadedBytes = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .timeout(Duration.ofSeconds(60))
                        .block(Duration.ofSeconds(65));

                if (downloadedBytes != null) {
                    Files.write(partFilePath, downloadedBytes);
                    System.out.println("Successfully downloaded: " + partFileName + " to " + partFilePath.toAbsolutePath());
                } else {
                    System.err.println("Downloaded bytes were null for " + partFileName + " from " + url);
                }

            } catch (Exception e) { // Catch any exception during WebClient blocking or Files.write
                System.err.println("Failed to download or save " + partFileName + " from " + url + ": " + e.getMessage());
            }
        }
    }

    public void downloadFilesFromChannelMulThread(int requestId, Path downloadDir, String fileName, String channelId) throws Exception {

        downloadDir = Path.of(downloadDir.toString(), fileName);

        if (Files.isDirectory(downloadDir)) {
            try (Stream<Path> walk = Files.walk(downloadDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        Files.createDirectories(downloadDir);

        List<UploadedFile> messages = this._uploadFileRepository.findByRequestIdOrderByPartNumberAsc(requestId);

        int fileToProcess = messages.size() / 4;

        String token = this._environment.getProperty("discord.bot.token");
        String token2 = this._environment.getProperty("discord.bot.token2");
        String token3 = this._environment.getProperty("discord.bot.token3");
        String token4 = this._environment.getProperty("discord.bot.token4");

        DownloadUsingMultipleThreads up1 = new DownloadUsingMultipleThreads("Thread 1", messages, channelId, downloadDir, token, 1, fileToProcess, webClient);
        DownloadUsingMultipleThreads up2 = new DownloadUsingMultipleThreads("Thread 2", messages, channelId, downloadDir, token2, 1 + fileToProcess, fileToProcess * 2, webClient);
        DownloadUsingMultipleThreads up3 = new DownloadUsingMultipleThreads("Thread 3", messages, channelId, downloadDir, token3, 1 + fileToProcess * 2, fileToProcess * 3, webClient);
        DownloadUsingMultipleThreads up4 = new DownloadUsingMultipleThreads("Thread 4", messages, channelId, downloadDir, token4, 1 + fileToProcess * 3, fileToProcess * 4 + messages.size() % 4, webClient);

        up1.start();
        up2.start();
        up3.start();
        up4.start();

        try {
            while(up1.getState() != Thread.State.TERMINATED || up2.getState() != Thread.State.TERMINATED || up3.getState() != Thread.State.TERMINATED || up4.getState() != Thread.State.TERMINATED) {
                System.out.println(up1.getState());
                System.out.println(up2.getState());
                System.out.println(up3.getState());
                System.out.println(up4.getState());

                Thread.sleep(3000);
            }
        } catch (Exception e) {
            System.err.println("Exception caught! Stopping the other thread.");

            if (up1.isAlive()) {
                up1.stopGracefully();
            }
            if (up2.isAlive()) {
                up2.stopGracefully();
            }
            if (up3.isAlive()) {
                up3.stopGracefully();
            }
            if (up4.isAlive()) {
                up4.stopGracefully();
            }

            throw e;
        }

        try (Stream<Path> files = Files.list(downloadDir)) {
            if(files
                    .filter(p -> p.getFileName().toString().startsWith(fileName + ".part"))
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingInt(this::partNumber))
                    .toList().size() != messages.size()) {
                throw new Exception("Unable to validate file count");
            }
        }

    }

}
