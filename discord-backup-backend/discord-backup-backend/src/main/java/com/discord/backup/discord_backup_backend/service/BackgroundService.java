package com.discord.backup.discord_backup_backend.service;

import com.discord.backup.discord_backup_backend.entity.DownloadRequest;
import com.discord.backup.discord_backup_backend.entity.UploadRequest;
import com.discord.backup.discord_backup_backend.repository.DownloadRequestRepository;
import com.discord.backup.discord_backup_backend.repository.UploadRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BackgroundService {

    @Autowired
    private ZipSplitterService _zipSplitterService;

    @Autowired
    private ProcessSplitFilesService _processSplitFileService;

    @Autowired
    private UploadRequestRepository _uploadRequestRepository;

    @Autowired
    private DownloadRequestRepository _downloadRequestRepository;

    @Scheduled(fixedRate = 6000)
    public void checkPendingSplitExecutions() {
        try {
            List<UploadRequest> splitRequests = getPendingUploadRequestsByStatus("INITIATED");

            for(UploadRequest req: splitRequests) {
                Path inputFile = Path.of(req.getInputPath(), req.getFileName());

                if(!req.getIsFolder() && !inputFile.toString().contains(".zip")) {
                    throw new Exception("Path should be a zip!!!");
                }

                if(req.getIsFolder()) {
                    try {
                        Path zipPath = this._zipSplitterService.convertFolderToZip(Path.of(req.getInputPath()));

                        req.setFileName(zipPath.getFileName().toString());
                        req.setInputPath(zipPath.getParent().toString());
                        req.setIsFolder(false);
                        req.setModifiedTs(LocalDateTime.now());

                        this._uploadRequestRepository.save(req);
                    }
                    catch (Exception e) {
                        req.setStatus("INVALID");
                        this._uploadRequestRepository.save(req);

                        throw e;
                    }
                }

                inputFile = Path.of(req.getInputPath(), req.getFileName());
                Path outputDir = Path.of(req.getOutputDir());

                if(!inputFile.toString().contains(".zip") && !req.getIsFolder()) {
                    throw new Exception("File should be a zip!!!");
                }

                this._zipSplitterService.splitZipFileIntoChunks(inputFile, outputDir, 10000000);

                req.setStatus("SPLITTED");
                req.setModifiedTs(LocalDateTime.now());

                this._uploadRequestRepository.save(req);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 6000)
    public void checkPendingUploads() {
        try {
            List<UploadRequest> uploadRequests = getPendingUploadRequestsByStatus("SPLITTED");

            for(UploadRequest req: uploadRequests) {
                Path uploadDir = Path.of(req.getOutputDir());

//                this._processSplitFileService.uploadFilesToChannel(req.getRequestId(), uploadDir, req.getChannelId(), req.getFileName());
                this._processSplitFileService.uploadFilesToChannelMulThread(req.getRequestId(), uploadDir, req.getChannelId(), req.getFileName());

                req.setStatus("PROCESSED");
                req.setModifiedTs(LocalDateTime.now());

                this._uploadRequestRepository.save(req);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 6000)
    public void checkPendingDownloads() {
        try {
            List<DownloadRequest> downloadRequests = getPendingDownloadRequestsByStatus("INITIATED");

            for(DownloadRequest req: downloadRequests) {
                Path downloadDir = Path.of(req.getDownloadDir());

//                this._processSplitFileService.downloadFilesFromChannel(req.getRequestId(), downloadDir, req.getFileName(), req.getChannelId());
                this._processSplitFileService.downloadFilesFromChannelMulThread(req.getRequestId(), downloadDir, req.getFileName(), req.getChannelId());

                req.setStatus("DOWNLOADED");
                req.setModifiedTs(LocalDateTime.now());

                this._downloadRequestRepository.save(req);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 6000)
    public void checkPendingCombineExecutions() {
        try {
            List<DownloadRequest> downloadRequests = getPendingDownloadRequestsByStatus("DOWNLOADED");

            for(DownloadRequest req: downloadRequests) {
                Path downloadDir = Path.of(req.getDownloadDir(), req.getFileName());
                Path zipPath = Path.of(downloadDir.toString(), "/zip");

                this._zipSplitterService.combineChunksIntoFile(downloadDir, req.getFileName(), zipPath);

                req.setStatus("PROCESSED");
                req.setModifiedTs(LocalDateTime.now());

                this._downloadRequestRepository.save(req);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private List<UploadRequest> getPendingUploadRequestsByStatus(String status) {
        return this._uploadRequestRepository.findByStatus(status);
    }

    private List<DownloadRequest> getPendingDownloadRequestsByStatus(String status) {
        return this._downloadRequestRepository.findByStatus(status);
    }
}
