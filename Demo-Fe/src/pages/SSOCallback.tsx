import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch } from '../store/hook';
import { ssoExchange } from '../reducers/authSlice';

export const SsoCallback = () => {
    const [searchParams] = useSearchParams();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    useEffect(() => {


        const code = searchParams.get('code');
        const error = searchParams.get('error');

        if (error) {
            console.log("SSO Error:", error);
            navigate('/', { state: { error: error } });
            return;
        }

        // Case 2: Authorization Code Received
        if (code) {
            console.log("Exchanging SSO Code:", code);
            dispatch(ssoExchange(code))
                .unwrap()
                .then(() => {
                    navigate('/');
                })
                .catch((err) => {
                    console.error("SSO Exchange Failed:", err);

                });
        } else {
            navigate('/');
        }
    }, [searchParams, dispatch, navigate]);

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-900 text-white">
            <div className="text-center">
                <div className="w-16 h-16 border-4 border-t-purple-500 border-white/20 rounded-full animate-spin mx-auto mb-4"></div>
                <h2 className="text-xl font-semibold">Verifying SSO Session...</h2>
            </div>
        </div>
    );
};
