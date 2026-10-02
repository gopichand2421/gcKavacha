import {
  BrowserRouter,
  Navigate,
  Route,
  Routes
} from "react-router-dom";

import LoginPage from "../pages/LoginPage";
import DashboardPage from "../pages/DashboardPage";
import ProtectedRoute from "./ ProtectedRoute";

export default function AppRoutes() {

  return (
    <BrowserRouter>

      <Routes>

        {/* Public route */}
        <Route
          path="/login"
          element={<LoginPage />}
        />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>

          <Route
            path="/dashboard"
            element={<DashboardPage />}
          />

        </Route>

        {/* Default route */}
        <Route
          path="/"
          element={
            <Navigate
              to="/dashboard"
              replace
            />
          }
        />

        {/* Unknown route */}
        <Route
          path="*"
          element={
            <Navigate
              to="/dashboard"
              replace
            />
          }
        />

      </Routes>

    </BrowserRouter>
  );
}
