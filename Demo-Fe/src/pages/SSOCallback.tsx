import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { authApi } from "../services/authApi";

export const SSOCallback = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const calledHelper = useRef(false);

    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    useEffect(() => {
        // Prevent Strict Mode double-invocation
        if (calledHelper.current) return;

        const code = searchParams.get("code");
        const error = searchParams.get("error");

        if (error === "login_required") {
            calledHelper.current = true;
            navigate("/login?sso_done=true", { replace: true });
            return;
        }

        if (code) {
            calledHelper.current = true;
            console.log("Exchanging SSO Code:", code);

            authApi.exchangeToken(code)
                .then((data) => {
                    console.log("SSO Success!");
                    localStorage.setItem("accessToken", data.accessToken);
                    localStorage.setItem("refreshToken", data.refreshToken);
                    // Force full reload to ensure app initialization (validateSession) runs with new token
                    window.location.href = "/";
                })
                .catch((err) => {
                    console.error("SSO Exchange Failed", err);
                    setErrorMsg("SSO Exchange Failed: " + (err.response?.data?.message || err.message));
                    // Don't navigate away, let user see error
                });
        } else {
            // Only redirect if NO params and NOT handled
            // But we need to be careful not to loop.
            // If just loaded /sso-callback with nothing?
            calledHelper.current = true;
            navigate("/login?sso_done=true");
        }
    }, [searchParams, navigate]);

    if (errorMsg) {
        return (
            <div style={{ padding: 50, color: 'red', textAlign: 'center' }}>
                <h2>Đăng nhập thất bại!</h2>
                <pre>{errorMsg}</pre>
                <button onClick={() => navigate("/login")}>Về trang đăng nhập</button>
            </div>
        );
    }

    return (
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '50px' }}>
            <h2>Đang xử lý đăng nhập...</h2>
        </div>
    );
};
