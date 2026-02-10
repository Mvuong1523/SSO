import { Button } from 'primereact/button';

const getCookie = (name: string): string | null => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
};

const LogoutButton = () => {
    const handleLogout = async () => {
        try {
            const refreshToken = getCookie('refresh');

            // Luôn gọi API logout với token trong body
            await fetch('http://localhost:8080/api/oauth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${refreshToken}`
                },
            });
        } catch (error) {
            console.error("Logout API failed", error);
        } finally {

            document.cookie = 'access=; path=/; max-age=0';
            document.cookie = 'refresh=; path=/; max-age=0';
            document.cookie = 'isAuthenticated=false; path=/; max-age=0';

            const redirectUri = encodeURIComponent(window.location.origin + '/students');
            window.location.href = `http://localhost:8080/login?redirect_uri=${redirectUri}`;
        }
    };

    return (
        <Button
            label="Đăng xuất"
            severity="danger"
            icon="pi pi-sign-out"
            onClick={handleLogout}
        />
    );
};

export default LogoutButton;
