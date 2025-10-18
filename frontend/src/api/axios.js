// src/axios.js
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080", // Ajusta si tu backend usa otro prefijo
});

// Interceptor: añade el token JWT automáticamente
api.interceptors.request.use((config) => {
  const token =
    localStorage.getItem("token") || sessionStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor: captura expiración del token y redirige al login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Borrar sesión y redirigir
      localStorage.clear();
      sessionStorage.clear();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
