import {
  Box,
  Button,
  Typography
} from "@mui/material";

import { useAuth } from "../auth/authContext";
import { useNavigate } from "react-router-dom";

export default function DashboardPage() {

  const { user, logout } = useAuth();

  const navigate = useNavigate();

  function handleLogout(){
    logout();
    navigate("/login", {
      replace: true
    });
  }

  return (
    <Box sx={{ p: 4 }}>

      <Typography
        variant="h4"
        component="h1"
        sx={{
          fontWeight: 700
        }}
      >
        GcKavacha Dashboard
      </Typography>

      <Typography
        variant="body1"
        sx={{ mt: 2 }}
      >
        Welcome, {user?.firstName}!
      </Typography>

      <Typography
        variant="body2"
        color="text.secondary"
        sx={{ mt: 1 }}
      >
        Logged in as: {user?.username}
      </Typography>
      <Button variant="outlined" color="error" onClick ={handleLogout}  sx={{mt:4}}>
        Logout
      </Button>

    </Box>
  );
}
