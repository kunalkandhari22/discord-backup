package com.discord.backup.discord_backup_backend.controller;

import com.discord.backup.discord_backup_backend.entity.DownloadRequest;
import com.discord.backup.discord_backup_backend.exception.InternalServerErrorException;
import com.discord.backup.discord_backup_backend.service.DownloadRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/download")
public class DownloadRequestController {
    @Autowired
    private DownloadRequestService _downloadRequestService;

    @GetMapping("download-requests")
    public ResponseEntity<Map<String, Object>> getDownloadRequests(@RequestParam int page, @RequestParam int pageSize) {
        try {
            DownloadRequestService.PaginatedResult<DownloadRequest> result = this._downloadRequestService.getDownloadRequests(page, pageSize);

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

    @GetMapping("download-requests/{requestId}")
    public ResponseEntity<DownloadRequest> getDownloadRequests(@PathVariable int requestId) {
        try {
            Optional<DownloadRequest> result = this._downloadRequestService.getDownloadRequestById(requestId);

            if(result.isEmpty()) {
                throw new Exception("Invalid Request ID!!!");
            }

            return ResponseEntity.ok(result.get());
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to get request data: " + ex.getMessage());
        }
    }

    @PutMapping("download-requests/{requestId}")
    public ResponseEntity<String> editDownloadRequest(@PathVariable int requestId, @RequestBody DownloadRequest downloadRequest) {
        try {
            if(!this._downloadRequestService.editDownloadRequest(requestId, downloadRequest)) {
                throw new Exception("Error occurred while updating data!!!");
            }

            return ResponseEntity.ok("Data Updated Successfully!!!");
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to update request data: " + ex.getMessage());
        }
    }

    @PostMapping("download-requests")
    public ResponseEntity<String> addDownloadRequest(@RequestBody DownloadRequest downloadRequest) {
        try {
            Optional<DownloadRequest> reqExists = this._downloadRequestService.getDownloadRequestById(downloadRequest.getRequestId());

            if(reqExists.isPresent()) {
                throw new Exception("Request with same ID already exists!!!");
            }

            if(!this._downloadRequestService.addDownloadRequest(downloadRequest)) {
                throw new Exception("Error occurred while adding data!!!");
            }

            return ResponseEntity.ok("Data Added Successfully!!!");
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to add request data: " + ex.getMessage());
        }
    }

    @DeleteMapping("download-requests/{requestId}")
    public ResponseEntity<String> editDownloadRequest(@PathVariable int requestId) {
        try {
            if(!this._downloadRequestService.deleteDownloadRequest(requestId)) {
                throw new Exception("Error occurred while deleting data!!!");
            }

            return ResponseEntity.ok("Data Deleted Successfully!!!");
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to delete request data: " + ex.getMessage());
        }
    }
}
