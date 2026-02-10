import ProtectedRoute from "../components/ProtectedRoute";
import StudentManager from "../pages/StudentManager";

export const authRouter = {
  element: <ProtectedRoute />,
  children: [
    {
      path: '/students',
      element: <StudentManager />
    },
    
  ],
};
