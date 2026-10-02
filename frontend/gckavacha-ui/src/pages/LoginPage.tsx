import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  Paper,
  TextField,
  Typography
} from "@mui/material";

import {
  FormEvent,
  useState
} from "react";

import { useNavigate } from "react-router-dom";

import { useAuth } from "../auth/authContext";

export default function LoginPage() {

  const {
    login,
    isLoading
  } = useAuth();

  const navigate = useNavigate();

  const [username, setUsername] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [error, setError] =
    useState<string | null>(null);

  const [submitting, setSubmitting] =
    useState(false);

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {

    event.preventDefault();

    setError(null);

    if (!username.trim()) {
      setError("Username is required");
      return;
    }

    if (!password) {
      setError("Password is required");
      return;
    }

    try {

      setSubmitting(true);

      // Calls POST /api/auth/login
      // Stores JWT
      // Calls GET /api/auth/me
      // Updates AuthContext
      await login({
        username: username.trim(),
        password
      });

      // Login successful
      navigate("/dashboard", {
        replace: true
      });

    } catch (error) {

      console.error(
        "Login failed:",
        error
      );

      setError(
        "Invalid username or password"
      );

    } finally {

      setSubmitting(false);
    }
  }

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

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "background.default",
        padding: 2
      }}
    >
      <Container maxWidth="sm">

        <Paper
          elevation={4}
          sx={{
            padding: 4,
            borderRadius: 3
          }}
        >

          <Typography
            variant="h4"
            component="h1"
            gutterBottom
            sx={{
              textAlign: "center",
              fontWeight: 700
            }}
          >
            GcKavacha
          </Typography>

          <Typography
            variant="body1"
            color="text.secondary"
            sx={{
              textAlign: "center",
              mb: 3
            }}
          >
            Sign in to your reliability platform
          </Typography>

          {error && (
            <Alert
              severity="error"
              sx={{ mb: 2 }}
            >
              {error}
            </Alert>
          )}

          <Box
            component="form"
            onSubmit={handleSubmit}
            noValidate
          >

            <TextField
              fullWidth
              label="Username"
              value={username}
              onChange={(event) =>
                setUsername(event.target.value)
              }
              margin="normal"
              autoComplete="username"
              autoFocus
              disabled={submitting}
            />

            <TextField
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              margin="normal"
              autoComplete="current-password"
              disabled={submitting}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={submitting}
              sx={{ mt: 3 }}
            >
              {submitting ? (
                <CircularProgress
                  size={24}
                  color="inherit"
                />
              ) : (
                "Sign In"
              )}
            </Button>

          </Box>

        </Paper>

      </Container>
    </Box>
  );
}
