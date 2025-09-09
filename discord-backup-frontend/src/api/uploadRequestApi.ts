import type { PaginatedResponse } from "../utils/paginatedResponse";
import api from "./axiosConfig";

export interface UploadRequest {
  requestId?: number;
  inputPath: string;
  fileName?: string;
  isFolder: boolean;
  outputDir: string;
  channelId: string;
  status?: string;
}

export interface UploadedFile {
  requestId?: number;
  partNumber?: number;
  messageId?: string;
}

export const getUploadRequests = (page = 1, pageSize = 10) =>
  api.get<PaginatedResponse<UploadRequest>>(`/upload/upload-requests?page=${page}&pageSize=${pageSize}`);

export const getUploadRequestById = (requestId: number) =>
  api.get<UploadRequest>(`/upload/upload-requests/${requestId}`);

export const addUploadRequest = (uploadRequest: UploadRequest) =>
  api.post(`/upload/upload-requests`, uploadRequest);

export const editUploadRequest = (requestId: number, uploadRequest: UploadRequest) =>
  api.put(`/upload/upload-requests/${requestId}`, uploadRequest);

export const deleteUploadRequest = (requestId: number) =>
  api.delete(`/upload/upload-requests/${requestId}`);

export const getUploadedFiles = (page = 1, pageSize = 10, requestId: string) =>
  api.get<PaginatedResponse<UploadedFile>>(`/upload/uploaded-files?requestId=${requestId}&page=${page}&pageSize=${pageSize}`);
