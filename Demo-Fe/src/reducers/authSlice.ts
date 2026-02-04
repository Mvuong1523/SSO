import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import type { AuthResponse, AuthState, LoginRequest, RegisterRequest } from "../types/authModel";
import { authApi } from "../services/authApi";

const initialState: AuthState = {
    user: null,
    accessToken: localStorage.getItem("accessToken"),
    isAuthenticated: !!localStorage.getItem("accessToken"),
    loading: false,
    error: null,
};
export const register = createAsyncThunk(
    "auth/register",
    async (request: RegisterRequest): Promise<void> => {
        await authApi.register(request);
    }
);
export const login = createAsyncThunk<AuthResponse, LoginRequest>(
    "auth/login",
    async (request: LoginRequest): Promise<AuthResponse> => {
        const response = await authApi.login(request);
        localStorage.setItem("accessToken", response.accessToken);
        localStorage.setItem("refreshToken", response.refreshToken);
        return response;
    }
);
export const logout = createAsyncThunk(
    "auth/logout",
    async (refreshToken: string): Promise<void> => {
        await authApi.logout(refreshToken);
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
    }
);

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(register.fulfilled, (state) => {
                state.loading = false;
                state.error = null;
            })
            .addCase(register.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || "Registration failed";
            })
            .addCase(login.fulfilled, (state, action) => {
                state.loading = false;
                state.error = null;
                state.user = {
                    userId: action.payload.userId,
                    userUid: action.payload.userUid,
                    userType: action.payload.userType,
                    authProvider: action.payload.authProvider,
                }
                state.accessToken = action.payload.accessToken;
                state.isAuthenticated = true;
            })
            .addCase(login.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || "Login failed";
            })
            .addCase(logout.fulfilled, (state) => {
                state.loading = false;
                state.error = null;
                state.user = null;
                state.accessToken = null;
                state.isAuthenticated = false;
            })
            .addCase(logout.rejected, (state, action) => {
                state.loading = false;
                state.error = action.error.message || "Logout failed";
            });
    },
});
export default authSlice.reducer;
