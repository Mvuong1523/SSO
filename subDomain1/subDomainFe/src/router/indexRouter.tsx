import { Navigate } from 'react-router-dom';
import { authRouter } from './authRouter';

export const routes = [
  {
    path: '/',
    element: <Navigate to="/students" replace />,
  },
  authRouter,
  {
    path: '*',
    element: <Navigate to="/students" replace />,
  },
];
