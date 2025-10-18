import React, { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const navigate = useNavigate();
  const [token, setToken] = useState(
    localStorage.getItem("token") || sessionStorage.getItem("token") || null
  );

  useEffect(() => {
    const expiration =
      localStorage.getItem("token_exp") || sessionStorage.getItem("token_exp");
    if (expiration && Date.now() > parseInt(expiration, 10)) {
      logout();
    }
  }, []);

  const login = (newToken, remember) => {
    setToken(newToken);
    const payload = JSON.parse(atob(newToken.split(".")[1]));
    const expirationTime = payload.exp * 1000;
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem("token", newToken);
    storage.setItem("token_exp", expirationTime.toString());
  };

  const logout = () => {
    setToken(null);
    localStorage.clear();
    sessionStorage.clear();
    navigate("/login");
  };

  return (
    <AuthContext.Provider value={{ token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
