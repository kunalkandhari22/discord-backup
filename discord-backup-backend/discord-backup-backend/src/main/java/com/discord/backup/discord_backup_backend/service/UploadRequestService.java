package com.discord.backup.discord_backup_backend.service;

import com.discord.backup.discord_backup_backend.entity.UploadRequest;
import com.discord.backup.discord_backup_backend.entity.UploadedFile;
import com.discord.backup.discord_backup_backend.repository.UploadRequestRepository;
import com.discord.backup.discord_backup_backend.repository.UploadedFileRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UploadRequestService {
    @Autowired
    private UploadRequestRepository _uploadRequestRepository;

    @Autowired
    private UploadedFileRepository _uploadedFileRepository;

    public PaginatedResult<UploadRequest> getUploadRequests(int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, Sort.by("requestId").ascending());

        Page<UploadRequest> pages =  this._uploadRequestRepository.findAll(pageable);

        List<UploadRequest> req = pages.getContent();

        return new PaginatedResult<UploadRequest>(req, pages.getTotalElements());
    }

    public Optional<UploadRequest> getUploadRequestById(int requestId) {
        return this._uploadRequestRepository.findById(requestId);
    }

    public boolean editUploadRequest(int requestId, UploadRequest uploadRequest) throws Exception {

        if(requestId != uploadRequest.getRequestId())
            throw new Exception("Invalid Request ID!!!");

        UploadRequest uploadRequestDb = this._uploadRequestRepository.findById(requestId).orElse(null);

        if(uploadRequestDb == null)
            throw new Exception("Invalid Request ID!!!");

        uploadRequestDb.setInputPath(uploadRequest.getInputPath());
        uploadRequestDb.setFileName(uploadRequest.getFileName());
        uploadRequestDb.setIsFolder(uploadRequest.getIsFolder());
        uploadRequestDb.setOutputDir(uploadRequest.getOutputDir());
        uploadRequestDb.setChannelId(uploadRequest.getChannelId());
        uploadRequestDb.setStatus(uploadRequest.getStatus());
        uploadRequestDb.setModifiedTs(LocalDateTime.now());

        this._uploadRequestRepository.save(uploadRequestDb);

        return true;
    }

    public boolean addUploadRequest(UploadRequest uploadRequest) throws Exception {

        if(uploadRequest.getRequestId() != null) {
            throw new Exception("Request ID should be null!!!");
        }

        uploadRequest.setModifiedTs(LocalDateTime.now());
        uploadRequest.setCreationTs(LocalDateTime.now());

        this._uploadRequestRepository.save(uploadRequest);

        return true;
    }

    public boolean deleteUploadRequest(int requestId) throws Exception {
        if(this._uploadRequestRepository.findById(requestId).orElse(null) == null)
            throw new Exception("Invalid Request ID!!!");

        this._uploadRequestRepository.deleteById(requestId);

        return true;
    }

    public PaginatedResult<UploadedFile> getUploadedFilesByRequestId(int requestId, int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, Sort.by("partNumber").ascending());

        Page<UploadedFile> pages =  this._uploadedFileRepository.findByRequestId(requestId, pageable);

        List<UploadedFile> req = pages.getContent();

        return new PaginatedResult<UploadedFile>(req, pages.getTotalElements());
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
