import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/authModel";
import axios from 'axios';

// Centralized Login Domain
const BASE_URL = "http://login-center.com:8080/api/auth";

export const axiosInstance = axios.create({
    baseURL: BASE_URL,
    withCredentials: true, // Important for Cookies
});

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
    },

    exchangeToken: async (code: string): Promise<AuthResponse> => {
        const response = await axiosInstance.post('/auth/sso/exchange', { code });
        return response.data;
    }
};