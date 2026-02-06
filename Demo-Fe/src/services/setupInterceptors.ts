
import { axiosInstance } from "./authApi";
import { logoutUser, refreshToken } from "../reducers/authSlice";

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });

    failedQueue = [];
};

export const setupInterceptors = (store: any) => {
    axiosInstance.interceptors.response.use(
        (response) => {
            return response;
        },
        async (error) => {
            const originalRequest = error.config;

            if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url?.includes('/oauth/refresh')) {
                if (isRefreshing) {
                    return new Promise(function (resolve, reject) {
                        failedQueue.push({ resolve, reject });
                    }).then(token => {
                        originalRequest.headers['Authorization'] = 'Bearer ' + token;
                        return axiosInstance(originalRequest);
                    }).catch(err => {
                        return Promise.reject(err);
                    });
                }

                originalRequest._retry = true;
                isRefreshing = true;

                const state = store.getState();
                const token = state.authStore.refreshToken;

                if (!token) {
                    store.dispatch(logoutUser());
                    return Promise.reject(error);
                }

                try {
                    // Try to refresh token
                    const resultAction = await store.dispatch(refreshToken(token));

                    if (refreshToken.fulfilled.match(resultAction)) {
                        const newAccessToken = resultAction.payload.accessToken;
                        axiosInstance.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
                        originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;

                        processQueue(null, newAccessToken);
                        return axiosInstance(originalRequest);
                    } else {
                        throw new Error("Refresh failed");
                    }
                } catch (err) {
                    processQueue(err, null);
                    store.dispatch(logoutUser());
                    return Promise.reject(err);
                } finally {
                    isRefreshing = false;
                }
            }

            return Promise.reject(error);
        }
    );
};
