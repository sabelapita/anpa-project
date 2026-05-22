import React, { createContext, useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginFromServiceToken } from "../api/authApi";

const AUTH_TOKEN_KEY = "token";
const AUTH_EXPIRATION_KEY = "token_exp";
const AUTH_USER_KEY = "user";

export const AuthContext = createContext();

const getStorageWithToken = () => {
  if (localStorage.getItem(AUTH_TOKEN_KEY)) {
    return localStorage;
  }

  if (sessionStorage.getItem(AUTH_TOKEN_KEY)) {
    return sessionStorage;
  }

  return null;
};

const clearAuthStorage = () => {
  [localStorage, sessionStorage].forEach((storage) => {
    storage.removeItem(AUTH_TOKEN_KEY);
    storage.removeItem(AUTH_EXPIRATION_KEY);
    storage.removeItem(AUTH_USER_KEY);
  });
};

const decodeTokenExpiration = (token) => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.exp ? payload.exp * 1000 : null;
  } catch {
    return null;
  }
};

const readStoredSession = () => {
  const storage = getStorageWithToken();

  if (!storage) {
    return { token: null, user: null };
  }

  const token = storage.getItem(AUTH_TOKEN_KEY);
  const expiration = Number(storage.getItem(AUTH_EXPIRATION_KEY));

  if (!token || (expiration && Date.now() > expiration)) {
    clearAuthStorage();
    return { token: null, user: null };
  }

  const storedUser = storage.getItem(AUTH_USER_KEY);
  let user = null;

  try {
    user = storedUser ? JSON.parse(storedUser) : null;
  } catch {
    clearAuthStorage();
    return { token: null, user: null };
  }

  return { token, user };
};

const persistSession = ({ token, user }, remember) => {
  clearAuthStorage();

  const storage = remember ? localStorage : sessionStorage;
  const expiration = decodeTokenExpiration(token);

  storage.setItem(AUTH_TOKEN_KEY, token);

  if (expiration) {
    storage.setItem(AUTH_EXPIRATION_KEY, expiration.toString());
  }

  if (user) {
    storage.setItem(AUTH_USER_KEY, JSON.stringify(user));
  }
};

export const AuthProvider = ({ children }) => {
  const navigate = useNavigate();
  const initialSession = useMemo(readStoredSession, []);
  const [token, setToken] = useState(initialSession.token);
  const [user, setUser] = useState(initialSession.user);
  const [loadingSession, setLoadingSession] = useState(Boolean(initialSession.token));

  const logout = useCallback(
    (redirect = true) => {
      clearAuthStorage();
      setToken(null);
      setUser(null);

      if (redirect) {
        navigate("/login", { replace: true });
      }
    },
    [navigate]
  );

  const login = useCallback((authenticatedUser, remember = false) => {
    const nextToken = authenticatedUser?.serviceToken;
    const nextUser = authenticatedUser?.user;

    if (!nextToken) {
      throw new Error("Missing service token");
    }

    persistSession({ token: nextToken, user: nextUser }, remember);
    setToken(nextToken);
    setUser(nextUser);
  }, []);

  useEffect(() => {
    if (!token) {
      setLoadingSession(false);
      return;
    }

    const storage = getStorageWithToken();
    const expiration = Number(storage?.getItem(AUTH_EXPIRATION_KEY));

    if (expiration && Date.now() > expiration) {
      logout(false);
      setLoadingSession(false);
      return;
    }

    if (user) {
      setLoadingSession(false);
      return;
    }

    loginFromServiceToken()
      .then((response) => {
        const remember = storage === localStorage;
        login(response.data, remember);
      })
      .catch(() => logout(false))
      .finally(() => setLoadingSession(false));
  }, [login, logout, token, user]);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      isAdmin: user?.role === "ADMIN",
      isMember: user?.membershipStatus === "MEMBER",
      loadingSession,
      login,
      logout,
    }),
    [loadingSession, login, logout, token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
