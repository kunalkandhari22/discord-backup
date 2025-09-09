import type { PaginatedResponse } from "../utils/paginatedResponse";
import api from "./axiosConfig";

export interface Channel {
  channelId: string;
  channelName?: string;
}

export interface CreateChannel {
  serverId: string;
  channelName: string;
}

export const getChannelsUsingPagination = (page = 1, pageSize = 10) =>
  api.get<PaginatedResponse<Channel>>(`/channel/allUsingPagination?page=${page}&pageSize=${pageSize}`);

export const getAllChannels = () =>
  api.get<Channel[]>(`/channel/all`);

export const getChannelById = (requestId: number) =>
  api.get<Channel>(`/channel/${requestId}`);

export const addChannel = (serverId: string, channelName: string) =>
  api.post(`/discord/create-channel/${serverId}?channelName=${channelName}`);

export const deleteChannelApi = (channelId: string) =>
  api.delete(`/discord/delete-channel/${channelId}`);