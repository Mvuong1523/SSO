import React, { useEffect, useState } from 'react';
import { useAppDispatch } from '../store/hook';
import { logout } from '../reducers/authSlice';
import { authApi } from '../services/authApi';

export const HomePage = () => {
    const dispatch = useAppDispatch();
    const [profile, setProfile] = useState<any>(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const data = await authApi.getProfile();
                setProfile(data);
            } catch (error) {
                console.error("Failed to fetch profile", error);
                // Nếu lỗi 401 thì axios interceptor đã tự redirect rồi
            }
        };
        fetchProfile();
    }, []);

    const handleLogout = async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
            try {
                await dispatch(logout(refreshToken)).unwrap();
            } catch (error) {
                console.error("Logout failed", error);
            }
        }
        localStorage.clear();
        window.location.reload();
    };

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh',
            fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
            <h1 style={{ color: '#2c3e50', marginBottom: '1rem' }}>
                Chào mừng đến với hệ thống!
            </h1>
            <p style={{ color: '#7f8c8d', marginBottom: '2rem' }}>
                {profile ? `Xin chào, ${profile.email} (${profile.userType})` : 'Đang kiểm tra token...'}
            </p>
            <button
                onClick={handleLogout}
                style={{
                    padding: '12px 24px',
                    backgroundColor: '#e74c3c',
                    color: 'white',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    transition: 'background 0.2s'
                }}
                onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#c0392b'}
                onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#e74c3c'}
            >
                Đăng xuất
            </button>
        </div>
    );
};
