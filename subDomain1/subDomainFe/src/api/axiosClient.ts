import axios from 'axios';

const getCookie = (name: string): string | null => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
};

const axiosClient = axios.create({
    baseURL: 'http://localhost:8081/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosClient.interceptors.request.use(
    (config) => {
        const token = getCookie('access');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);


axiosClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        console.log(originalRequest);

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            const refreshToken = getCookie('refresh');

            if (refreshToken) {
                try {
                    // Gọi auth server để refresh token
                    const response = await axios.post('http://localhost:8080/api/oauth/refresh', {},
                        {
                            headers: {
                                'Authorization': `Bearer ${refreshToken}`
                            }
                        }
                    );

                    const { accessToken } = response.data;

                    // Lưu token mới vào cookie (session cookie)
                    document.cookie = `access=${accessToken}; path=/`;


                    originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                    return axiosClient(originalRequest);
                } catch (refreshError) {

                    document.cookie = 'access=; path=/; max-age=0';
                    document.cookie = 'refresh=; path=/; max-age=0';
                    document.cookie = 'isAuthenticated=false; path=/; max-age=0';

                    window.location.href = '/';
                    return Promise.reject(refreshError);
                }
            } else {

                document.cookie = 'access=; path=/; max-age=0';
                document.cookie = 'refresh=; path=/; max-age=0';
                document.cookie = 'isAuthenticated=false; path=/; max-age=0';

                window.location.href = '/';
                return Promise.reject(error);
            }
        }

        // Xử lý 403 (Forbidden) - Không có quyền hoặc token invalid
        if (error.response?.status === 403) {

            // Xóa cookies và logout
            document.cookie = 'access=; path=/; max-age=0';
            document.cookie = 'refresh=; path=/; max-age=0';
            document.cookie = 'isAuthenticated=false; path=/; max-age=0';

            window.location.href = '/';
            return Promise.reject(error);
        }

        return Promise.reject(error);
    }
);

export default axiosClient;
