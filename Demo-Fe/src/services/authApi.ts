import axios from 'axios';

// Backend URL
const BASE_URL = "http://localhost:8080/api";

export const axiosInstance = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // Cookie
});

// Interfaces
export interface LoginRequest {
    username?: string;
    email?: string; // Fallback
    password?: string;
    pwd?: string; // Fallback
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    userUid: number;
    userType: string;
}

export const authApi = {
    // 1. Local Login (Fallback or Primary if Login UI is here)
    login: async (data: LoginRequest) => {
        // Backend expects 'username' (mapped from email if needed)
        const payload = {
            username: data.username || data.email,
            password: data.password || data.pwd
        };
        console.log("DEBUG: Calling Login API with:", payload); // Debug Payload
        const response = await axiosInstance.post('/auth/login', payload);
        return response.data;
    },

    // 2. Exchange Code for Token
    exchangeToken: async (code: string) => {
        const response = await axiosInstance.post('/oauth/token', { code });
        return response.data;
    },

    // 3. Refresh Token
    refreshToken: async (refreshToken: string) => {
        const response = await axiosInstance.post('/oauth/refresh', { refreshToken });
        return response.data;
    },

    // 4. Logout
    logout: async (refreshToken?: string) => {
        return axiosInstance.post('/oauth/logout', { refreshToken });
    },

    // 5. Initiate SSO (Helper)
    initiateSso: (redirectUrl: string) => {
        const authUrl = `${BASE_URL}/oauth/authorize?redirect_uri=${encodeURIComponent(redirectUrl)}`;
        window.location.href = authUrl;
    }
};