import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
    children: React.ReactNode;
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
    const isAuthenticated = !!localStorage.getItem('accessToken');

    if (!isAuthenticated) {

        const ssoUrl = "http://login-center.com:8080/api/auth/sso/authorize";
        const callbackUrl = window.location.origin + "/sso-callback";
        const targetUrl = ssoUrl + "?redirectUrl=" + encodeURIComponent(callbackUrl);

        window.location.href = targetUrl;
        return null;
    }

    return <>{children}</>;
};
