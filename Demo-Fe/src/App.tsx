import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './App.css';

// Pages & Components
import { SsoCallback } from './pages/SsoCallback';
import { LoginCentral } from './pages/LoginCentral';
import StudentManager from './pages/StudentManager';
import LoginCheck from './components/LoginCheck';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/sso-callback" element={<SsoCallback />} />
        {/* Central Login UI for SSO */}
        <Route path="/login-central" element={<LoginCentral />} />

        {/* Protected Root */}
        <Route path="/" element={<LoginCheck><StudentManager /></LoginCheck>} />

        {/* Catch all - Redirect to Home/Protected */}
        <Route path="*" element={<LoginCheck><StudentManager /></LoginCheck>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
