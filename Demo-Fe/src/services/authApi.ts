import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/authModel";
import axiosInstance from "./axiosInstance";

export const authApi = {
    register: async (request: RegisterRequest): Promise<void> => {
        await axiosInstance.post('/auth/register', request);
    },

    login: async (request: LoginRequest): Promise<AuthResponse> => {
        const response = await axiosInstance.post<AuthResponse>('/auth/login', request);
        return response.data;
    },

    refresh: async (refreshToken: string): Promise<AuthResponse> => {
        const response = await axiosInstance.post<AuthResponse>('/auth/refresh', { refreshToken });
        return response.data;
    },

    logout: async (refreshToken: string): Promise<void> => {
        await axiosInstance.post('/auth/sso/logout', { refreshToken });
    },

    getProfile: async (): Promise<any> => {
        const response = await axiosInstance.get('/auth/profile');
        return response.data;
    },

    checkSession: async (): Promise<any> => {
        const response = await axiosInstance.get('/auth/sso/check');
        return response.data;
    },

    issueToken: async (): Promise<AuthResponse> => {
        const response = await axiosInstance.get('/auth/sso/token?appId=frontend&redirectUrl=');
        return response.data;
    }
};