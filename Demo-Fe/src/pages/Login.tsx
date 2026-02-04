import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../reducers/authSlice";
import { useAppDispatch, useAppSelector } from "../store/hook";

export const LoginPage = () => {
    const [formData, setFormData] = useState({
        email: "",
        pwd: ""
    });

    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const { loading, error } = useAppSelector(state => state.authStore);

    // Auto-login check via SSO Session
    useEffect(() => {
        const checkSSO = async () => {
            // Only check if not already logged in locally
            if (!localStorage.getItem('accessToken')) {
                try {
                    const { authApi } = await import("../services/authApi");
                    const checkData = await authApi.checkSession();

                    if (checkData.authenticated) {
                        const tokenData = await authApi.issueToken();
                        localStorage.setItem('accessToken', tokenData.accessToken);
                        localStorage.setItem('refreshToken', tokenData.refreshToken); // If issues token returns it
                        // Update Redux state manually or reload to let App handle it
                        window.location.reload();
                    }
                } catch (e) {
                    // Session invalid, stay on login page
                }
            }
        };
        checkSSO();
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await dispatch(login(formData)).unwrap();
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);
            navigate("/");
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
                    type="submit"
                    disabled={loading}
                    style={{ width: '100%', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', cursor: loading ? 'not-allowed' : 'pointer' }}
                >
                    Đăng nhập
                </button>
            </form>
        </div>
    );
};