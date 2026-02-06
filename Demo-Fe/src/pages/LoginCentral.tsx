
import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { authApi } from '../services/authApi';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Message } from 'primereact/message';

// --- Central Login Page (Replaces Thymeleaf) ---
export const LoginCentral = () => {
    const [searchParams] = useSearchParams();
    const redirectUri = searchParams.get('redirect_uri') || window.location.origin;

    const [email, setEmail] = useState(''); // Use email/username
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await authApi.login({ email, password });

            const authUrl = `http://localhost:8080/api/oauth/authorize?redirect_uri=${encodeURIComponent(redirectUri)}`;
            window.location.href = authUrl;

        } catch (err: any) {
            console.error("Login Failed:", err);
            setError(err.response?.data?.message || "Đăng nhập thất bại. Vui lòng kiểm tra lại!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <div className="bg-white p-8 rounded-lg shadow-md w-96">
                <div className="text-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-800">SSO Login Center</h2>
                    <p className="text-gray-500 text-sm">Đăng nhập một nơi, truy cập mọi nơi</p>
                </div>

                {error && <Message severity="error" text={error} className="w-full mb-4" />}

                <form onSubmit={handleLogin} className="flex flex-col gap-4">
                    <div className="flex flex-col gap-2">
                        <label className="font-medium text-gray-700">Email / Tên đăng nhập</label>
                        <InputText
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="Nhập email..."
                            required
                        />
                    </div>

                    <div className="flex flex-col gap-2">
                        <label className="font-medium text-gray-700">Mật khẩu</label>
                        <Password
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            feedback={false}
                            toggleMask
                            placeholder="Nhập mật khẩu..."
                            required
                            className="w-full"
                            inputClassName="w-full"
                        />
                    </div>

                    <Button
                        label="Đăng Nhập"
                        icon="pi pi-sign-in"
                        loading={loading}
                        type="submit"
                        className="mt-2"
                    />
                </form>

                <div className="mt-4 text-center text-xs text-gray-400">
                    <p>Redirecting to: {redirectUri}</p>
                </div>
            </div>
        </div>
    );
};
