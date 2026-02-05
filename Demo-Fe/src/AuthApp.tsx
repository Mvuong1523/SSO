import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/Login';
import { HomePage } from './pages/Home';
import { SSOCallback } from './pages/SSOCallback';
import { ProtectedRoute } from './components/ProtectedRoute';
import { useEffect } from 'react';
import { useAppDispatch } from './store/hook';
import { validateSession } from './reducers/authSlice';

function AuthApp() {
    const dispatch = useAppDispatch();

    useEffect(() => {
        // Validate session on app load (e.g. after refresh)
        const token = localStorage.getItem("accessToken");
        if (token) {
            dispatch(validateSession());
        }
    }, [dispatch]);

    return (
        <BrowserRouter>
            <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/sso-callback" element={<SSOCallback />} />

                {/* Protected Routes */}
                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <HomePage />
                        </ProtectedRoute>
                    }
                />

                {/* Catch all - Redirect to Home */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default AuthApp;
