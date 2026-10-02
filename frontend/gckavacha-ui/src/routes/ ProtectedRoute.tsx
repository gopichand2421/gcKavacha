import {
  Navigate,
  Outlet,
  useLocation
} from "react-router-dom";

import { CircularProgress, Box } from "@mui/material";

import { useAuth } from "../auth/authContext";

export default function ProtectedRoute() {

  const {
    isAuthenticated,
    isLoading
  } = useAuth();

  const location = useLocation();

  if (isLoading) {
    return (
      <Box
        sx={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center"
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: location.pathname
        }}
      />
    );
  }

  return <Outlet />;
}
