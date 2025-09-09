import type { PaginatedResponse } from "../utils/paginatedResponse";
import api from "./axiosConfig";

export interface DownloadRequest {
  requestId?: number;
  downloadDir: string;
  fileName?: string;
  channelId: string;
  status?: string;
}

export const getDownloadRequests = (page = 1, pageSize = 10) =>
  api.get<PaginatedResponse<DownloadRequest>>(`/download/download-requests?page=${page}&pageSize=${pageSize}`);

export const getDownloadRequestById = (requestId: number) =>
  api.get<DownloadRequest>(`/download/download-requests/${requestId}`);

export const addDownloadRequest = (downloadRequest: DownloadRequest) =>
  api.post(`/download/download-requests`, downloadRequest);

export const editDownloadRequest = (requestId: number, downloadRequest: DownloadRequest) =>
  api.put(`/download/download-requests/${requestId}`, downloadRequest);

export const deleteDownloadRequest = (requestId: number) =>
  api.delete(`/download/download-requests/${requestId}`);
