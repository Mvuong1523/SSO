export interface User {
    userId: string;
    userUid: number;
    userType: string;
    authProvider: string;
}
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    userId: string;
    userUid: number;
    userType: string;
    authProvider: string;
}
export interface LoginRequest {
    email: string;
    pwd: string;
}
export interface RegisterRequest {
    userId: string;
    password: string;
    fullName: string;
    phoneNumber?: string;
}
export interface AuthState {
    user: User | null;
    accessToken: string | null;
    isAuthenticated: boolean;
    loading: boolean;
    error: string | null;
}