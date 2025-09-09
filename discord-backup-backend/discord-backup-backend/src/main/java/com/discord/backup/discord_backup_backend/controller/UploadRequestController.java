package com.discord.backup.discord_backup_backend.controller;

import com.discord.backup.discord_backup_backend.entity.UploadRequest;
import com.discord.backup.discord_backup_backend.entity.UploadedFile;
import com.discord.backup.discord_backup_backend.exception.InternalServerErrorException;
import com.discord.backup.discord_backup_backend.service.UploadRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/upload")
public class UploadRequestController {
    @Autowired
    private UploadRequestService _uploadRequestService;

    @GetMapping("upload-requests")
    public ResponseEntity<Map<String, Object>> getUploadRequests(@RequestParam int page, @RequestParam int pageSize) {
        try {
            UploadRequestService.PaginatedResult<UploadRequest> result = this._uploadRequestService.getUploadRequests(page, pageSize);

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

    @GetMapping("upload-requests/{requestId}")
    public ResponseEntity<UploadRequest> getUploadRequestById(@PathVariable int requestId) {
        try {
            Optional<UploadRequest> result = this._uploadRequestService.getUploadRequestById(requestId);

            if(result.isEmpty()) {
                throw new Exception("Invalid Request ID!!!");
            }

            return ResponseEntity.ok(result.get());
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to get request data: " + ex.getMessage());
        }
    }

    @PutMapping("upload-requests/{requestId}")
    public ResponseEntity<String> editUploadRequest(@PathVariable int requestId, @RequestBody UploadRequest uploadRequest) {
        try {
            if(!this._uploadRequestService.editUploadRequest(requestId, uploadRequest)) {
                throw new Exception("Error occurred while updating data!!!");
            }

            return ResponseEntity.ok("Data Updated Successfully!!!");
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to update request data: " + ex.getMessage());
        }
    }

    @PostMapping("upload-requests")
    public ResponseEntity<String> addUploadRequest(@RequestBody UploadRequest uploadRequest) {
        try {
            if(!this._uploadRequestService.addUploadRequest(uploadRequest)) {
                throw new Exception("Error occurred while adding data!!!");
            }

            return ResponseEntity.ok("Data Added Successfully!!!");
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to add request data: " + ex.getMessage());
        }
    }

    @DeleteMapping("upload-requests/{requestId}")
    public ResponseEntity<String> deleteUploadRequest(@PathVariable int requestId) {
        try {
            if(!this._uploadRequestService.deleteUploadRequest(requestId)) {
                throw new Exception("Error occurred while deleting data!!!");
            }

            return ResponseEntity.ok("Data Deleted Successfully!!!");
        }
        catch (Exception ex) {
            throw new InternalServerErrorException("Failed to delete request data: " + ex.getMessage());
        }
    }

    @GetMapping("uploaded-files")
    public ResponseEntity<Map<String, Object>> getUploadedFilesByRequestId(@RequestParam int requestId, @RequestParam int page, @RequestParam int pageSize) {
        try {
            UploadRequestService.PaginatedResult<UploadedFile> result = this._uploadRequestService.getUploadedFilesByRequestId(requestId, page, pageSize);

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
}
