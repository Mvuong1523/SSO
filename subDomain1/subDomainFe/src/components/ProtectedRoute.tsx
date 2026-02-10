import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';

const getCookie = (name: string): string | null => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
};

const ProtectedRoute = () => {
    const location = useLocation();
    const [isChecking, setIsChecking] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {


        const urlParams = new URLSearchParams(window.location.search);
        const accessToken = urlParams.get('access_token');
        const refreshToken = urlParams.get('refresh_token');


        if (accessToken && refreshToken) {

            document.cookie = 'access=; path=/; max-age=0';
            document.cookie = 'refresh=; path=/; max-age=0';


            document.cookie = `access=${accessToken}; path=/`;
            document.cookie = `refresh=${refreshToken}; path=/`;
            document.cookie = `isAuthenticated=true; path=/`;


            window.history.replaceState({}, document.title, window.location.pathname);

            setIsAuthenticated(true);
        } else {
            // Check cookie isAuthenticated
            const authStatus = getCookie('isAuthenticated');
            setIsAuthenticated(authStatus === 'true');
        }

        setIsChecking(false);
    }, []);

    if (isChecking) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
                <div>Loading...</div>
            </div>
        );
    }

    if (!isAuthenticated) {
        const ssoUrl = "http://localhost:8080/api/oauth/authorize";
        const redirectUri = window.location.href;
        window.location.href = ssoUrl + "?redirect_uri=" + encodeURIComponent(redirectUri);
        return null;
    }

    return <Outlet />;
};

export default ProtectedRoute;
