import React, { useState, useRef } from 'react';
import { ConfirmDialog } from 'primereact/confirmdialog';
import { Toast } from 'primereact/toast';
import { Button } from 'primereact/button';
import { useAppDispatch, useAppSelector } from '../store/hook';
import { logoutUser } from '../reducers/authSlice';
import { axiosInstance } from '../services/authApi'; // Corrected import source as per usage in App.tsx

export default function StudentManager() {
    const toast = useRef<Toast>(null)
    const dispatch = useAppDispatch();

    // State kept for compatibility/future use even if purely debug UI now

    const { user } = useAppSelector(state => state.authStore);

    const handleLogout = () => {
        dispatch(logoutUser());
    };

    return (
        <>
            <Toast ref={toast} />
            <ConfirmDialog />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <span>Xin chào, {user?.userUid}</span>
                    <Button label="Đăng xuất" severity="warning" size="small" onClick={handleLogout} />
                </div>
            </div>

            {/* --- Token Debug Info --- */}
            <div className="bg-gray-100 p-4 rounded mb-4 text-xs font-mono break-all border border-gray-300">
                <p className="mb-2"><strong>Access Token:</strong> {useAppSelector(state => state.authStore.accessToken)}</p>
                <p className="mb-2"><strong>Refresh Token:</strong> {useAppSelector(state => state.authStore.refreshToken)}</p>
                <Button
                    label="Kiểm tra Token (Ping API)"
                    size="small"
                    severity="help"
                    outlined
                    style={{ marginTop: '10px' }}
                    onClick={async () => {
                        try {
                            toast.current?.show({ severity: 'info', summary: 'Đang kiểm tra...', life: 1000 });
                            // Call Secured API via Axios (to trigger Interceptors if 401)
                            const res = await axiosInstance.get("/test/ping");
                            toast.current?.show({ severity: 'success', summary: 'Thành công', detail: res.data.message, life: 3000 });
                        } catch (e) {
                            // Interceptor handles 401s auto-logout. 
                            // If we reach here, it's likely a network error or 500.
                            toast.current?.show({ severity: 'error', summary: 'Lỗi', detail: 'Token hết hạn hoặc lỗi mạng!', life: 3000 });
                        }
                    }}
                />
            </div>

        </>
    )
}
