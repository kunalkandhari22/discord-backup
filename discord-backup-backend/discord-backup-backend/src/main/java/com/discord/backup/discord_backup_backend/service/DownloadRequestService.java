package com.discord.backup.discord_backup_backend.service;

import com.discord.backup.discord_backup_backend.entity.DownloadRequest;
import com.discord.backup.discord_backup_backend.repository.DownloadRequestRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DownloadRequestService {

    @Autowired
    private DownloadRequestRepository _downloadRequestRepository;

    public DownloadRequestService.PaginatedResult<DownloadRequest> getDownloadRequests(int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, Sort.by("requestId").ascending());

        Page<DownloadRequest> pages =  this._downloadRequestRepository.findAll((Pageable) pageable);

        List<DownloadRequest> req = pages.getContent();

        return new DownloadRequestService.PaginatedResult<DownloadRequest>(req, pages.getTotalElements());
    }

    public Optional<DownloadRequest> getDownloadRequestById(int requestId) {
        return this._downloadRequestRepository.findById(requestId);
    }

    public boolean editDownloadRequest(int requestId, DownloadRequest downloadRequest) throws Exception {

        if(requestId != downloadRequest.getRequestId())
            throw new Exception("Invalid Request ID!!!");

        DownloadRequest downloadRequestDb = this._downloadRequestRepository.findById(requestId).orElse(null);

        if(downloadRequestDb == null)
            throw new Exception("Invalid Request ID!!!");

        downloadRequestDb.setDownloadDir(downloadRequest.getDownloadDir());
        downloadRequestDb.setFileName(downloadRequest.getFileName());
        downloadRequestDb.setChannelId(downloadRequest.getChannelId());
        downloadRequestDb.setStatus(downloadRequest.getStatus());
        downloadRequestDb.setModifiedTs(LocalDateTime.now());

        this._downloadRequestRepository.save(downloadRequestDb);

        return true;
    }

    public boolean addDownloadRequest(DownloadRequest downloadRequest) throws Exception {
        downloadRequest.setModifiedTs(LocalDateTime.now());
        downloadRequest.setCreationTs(LocalDateTime.now());

        this._downloadRequestRepository.save(downloadRequest);

        return true;
    }

    public boolean deleteDownloadRequest(int requestId) throws Exception {
        if(this._downloadRequestRepository.findById(requestId).orElse(null) == null)
            throw new Exception("Invalid Request ID!!!");

        this._downloadRequestRepository.deleteById(requestId);

        return true;
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
