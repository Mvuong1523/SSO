import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { authApi, type LoginRequest, type AuthResponse } from "../services/authApi";

// --- Types ---
interface AuthState {
    user: any | null;
    accessToken: string | null;
    refreshToken: string | null;
    isAuthenticated: boolean;
    loading: boolean;
    error: string | null;
}

// Initial State: Load from LocalStorage
const storedAccessToken = localStorage.getItem("accessToken");
const storedRefreshToken = localStorage.getItem("refreshToken");

const initialState: AuthState = {
    user: null,
    accessToken: storedAccessToken,
    refreshToken: storedRefreshToken,
    isAuthenticated: !!storedAccessToken,
    loading: false,
    error: null,
};

// --- Helpers ---
const saveTokens = (data: AuthResponse) => {
    localStorage.setItem("accessToken", data.accessToken);
    if (data.refreshToken) {
        localStorage.setItem("refreshToken", data.refreshToken);
    }
};

const clearTokens = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
};

// --- Thunks ---

export const loginUser = createAsyncThunk(
    "auth/login",
    async (formData: LoginRequest, { rejectWithValue }) => {
        try {
            const response = await authApi.login(formData);
            saveTokens(response);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data?.message || "Login failed");
        }
    }
);

export const ssoExchange = createAsyncThunk(
    "auth/ssoExchange",
    async (code: string, { rejectWithValue }) => {
        try {
            const response = await authApi.exchangeToken(code);
            saveTokens(response);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data?.message || "SSO Exchange failed");
        }
    }
);

export const logoutUser = createAsyncThunk(
    "auth/logout",
    async (_, { getState }) => {
        const state = getState() as any;
        const token = state.authStore.refreshToken;
        try {
            await authApi.logout(token);
        } catch (e) {
            console.error(e);
        }
        clearTokens();
        return null; // Resolve
    }
);

export const refreshToken = createAsyncThunk(
    "auth/refreshToken",
    async (token: string, { rejectWithValue }) => {
        try {
            const response = await authApi.refreshToken(token);
            saveTokens(response);
            return response;
        } catch (err: any) {
            clearTokens();
            return rejectWithValue(err.response?.data?.message || "Refresh failed");
        }
    }
);

// --- Slice ---
const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            // Login
            .addCase(loginUser.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(loginUser.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.accessToken = action.payload.accessToken;
                state.refreshToken = action.payload.refreshToken;
                state.user = {
                    userUid: action.payload.userUid,
                    userType: action.payload.userType
                };
            })
            .addCase(loginUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // SSO Exchange
            .addCase(ssoExchange.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(ssoExchange.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.accessToken = action.payload.accessToken;
                state.refreshToken = action.payload.refreshToken;
                state.user = {
                    userUid: action.payload.userUid,
                    userType: action.payload.userType
                };
            })
            .addCase(ssoExchange.rejected, (state, action) => {
                state.loading = false;
                state.isAuthenticated = false;
                state.error = action.payload as string;
            })

            // Logout
            .addCase(logoutUser.fulfilled, (state) => {
                state.user = null;
                state.accessToken = null;
                state.refreshToken = null;
                state.isAuthenticated = false;
            })

            // Refresh Token
            .addCase(refreshToken.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.accessToken = action.payload.accessToken;
                state.refreshToken = action.payload.refreshToken;
            })
            .addCase(refreshToken.rejected, (state, action) => {
                state.loading = false;
                state.isAuthenticated = false;
                state.user = null;
                state.accessToken = null;
                state.refreshToken = null;
                state.error = action.payload as string;
            });
    },
});

export const { clearError } = authSlice.actions;
export default authSlice.reducer;
