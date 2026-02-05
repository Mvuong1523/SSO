import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../reducers/authSlice";
import { useAppDispatch, useAppSelector } from "../store/hook";
import { authApi } from "../services/authApi";

export const LoginPage = () => {
    const [formData, setFormData] = useState({
        email: "",
        pwd: ""
    });

    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const { loading, error, isAuthenticated } = useAppSelector(state => state.authStore);

    const queryParameters = new URLSearchParams(window.location.search);
    const redirectAfterLogin = queryParameters.get("redirect_after_login");
    const isSsoRedirect = queryParameters.get("start_sso") === "true";

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    // Check SSO Session on Mount (Federation Style)
    useEffect(() => {
        // If we have already checked SSO (sso_done=true coming from callback), just show form
        const ssoDone = queryParameters.get("sso_done") === "true";
        if (ssoDone || isSsoRedirect) return;

        // Otherwise, redirect to Identity Provider to check session
        console.log("Redirecting to Identity Provider for SSO Check...");

        // Centralized Login Domain
        const ssoUrl = "http://login-center.com:8080/api/auth/sso/authorize";
        const callbackUrl = window.location.origin + "/sso-callback";

        let targetUrl = ssoUrl + "?redirectUrl=" + encodeURIComponent(callbackUrl);

        window.location.href = targetUrl;
    }, [isSsoRedirect]);

    // REAL IMPLEMENTATION BELOW



    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        console.log("Login: Start");

        try {
            console.log("Login: Dispatching action...");
            const response = await dispatch(login(formData)).unwrap();
            console.log("Login: Success, payload received", response);

            console.log("Login: Navigating to Home (Full Reload)");
            window.location.href = "/";
        } catch (error) {
            console.error('Login failed:', error);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '100px auto', padding: '20px' }}>
            <h1>Đăng nhập</h1>
            <form onSubmit={handleLogin}>
                <div style={{ marginBottom: '15px' }}>
                    <label>Email:</label>
                    <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="Email"
                        required
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label>Mật khẩu:</label>
                    <input
                        type="password"
                        name="pwd"
                        value={formData.pwd}
                        onChange={handleChange}
                        placeholder="Password"
                        required
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}
                <button
                    type="button"
                    onClick={(e) => handleLogin(e as any)}
                    disabled={loading}
                    style={{ width: '100%', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', cursor: loading ? 'not-allowed' : 'pointer' }}
                >
                    Đăng nhập
                </button>
            </form>
        </div>
    );
};