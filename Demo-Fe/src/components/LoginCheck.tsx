import React, { useEffect } from 'react';
import { useAppSelector } from '../store/hook';
import { authApi } from '../services/authApi';

const LoginCheck = ({ children }: { children: React.ReactElement }) => {
    const { isAuthenticated, accessToken } = useAppSelector(state => state.authStore);

    useEffect(() => {
        if (!isAuthenticated && !accessToken) {
            // Redirect to Backend Authorize Endpoint directly
            const callbackUrl = window.location.origin + "/sso-callback";
            authApi.initiateSso(callbackUrl);
        }
    }, [isAuthenticated, accessToken]);

    if (!isAuthenticated) {
        return (
            <div className="flex h-screen items-center justify-center bg-gray-50">
                <div className="text-center">
                    <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">Redirecting to login...</p>
                </div>
            </div>
        );
    }

    return children;
};

export default LoginCheck;
