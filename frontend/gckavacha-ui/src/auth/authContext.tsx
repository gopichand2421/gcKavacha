import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useState
} from "react";

import {
  getCurrentUser,
  login as loginApi
} from "../api/authApi";

import { authStorage } from "./authStorage";

import {
  CurrentUser,
  LoginRequest
} from "../types/auth";

interface AuthContextValue {
  user: CurrentUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext =
  createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({
  children
}: AuthProviderProps) {

  const [user, setUser] =
    useState<CurrentUser | null>(null);

  const [isLoading, setIsLoading] =
    useState(true);

  useEffect(() => {

    async function restoreAuthentication() {

      const token = authStorage.getToken();

      if (!token) {
        setIsLoading(false);
        return;
      }

      try {

        const currentUser =
          await getCurrentUser();

        setUser(currentUser);

      } catch (error) {

        console.error(
          "Failed to restore authentication:",
          error
        );

        authStorage.clearToken();
        setUser(null);

      } finally {

        setIsLoading(false);
      }
    }

    restoreAuthentication();

  }, []);

  async function login(
    request: LoginRequest
  ): Promise<void> {

    // 1. Call POST /api/auth/login
    const response =
      await loginApi(request);

    // 2. Store JWT
    authStorage.setToken(response.token);

    // 3. Call GET /api/auth/me
    const currentUser =
      await getCurrentUser();

    // 4. Update React authentication state
    setUser(currentUser);
  }

  function logout(): void {

    authStorage.clearToken();
    setUser(null);
  }

  const value: AuthContextValue = {
    user,
    isAuthenticated: user !== null,
    isLoading,
    login,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {

  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error(
      "useAuth must be used inside AuthProvider"
    );
  }

  return context;
}

