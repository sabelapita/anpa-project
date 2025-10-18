import React, { useState, useContext } from "react";
import axios from "api/axios";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import "./LoginPage.css";

const LoginPage = () => {
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!userName || !password) {
      setError("Por favor, completa todos los campos.");
      return;
    }

    try {
      setLoading(true);
      const response = await axios.post("/users/login", { userName, password });
      login(response.data.serviceToken, remember);
      navigate("/");
    } catch (err) {
      setError("Usuario o contraseña incorrectos.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-bg d-flex justify-content-center align-items-center vh-100">
      <div className="card login-card shadow-lg p-4 border-0">
        <h3 className="text-center mb-4 fw-bold text-primary">Iniciar sesión</h3>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="userName" className="form-label">
              Usuario
            </label>
            <input
              id="userName"
              type="text"
              className="form-control"
              placeholder="Introduce tu usuario"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              autoComplete="userName"
            />
          </div>

          <div className="mb-3">
            <label htmlFor="password" className="form-label">
              Contraseña
            </label>
            <input
              id="password"
              type="password"
              className="form-control"
              placeholder="Introduce tu contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <div className="form-check mb-3 d-flex justify-content-between align-items-center">
            <div>
              <input
                type="checkbox"
                className="form-check-input"
                id="remember"
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
              <label className="form-check-label" htmlFor="remember">
                Recordar sesión
              </label>
            </div>
            <button
                type="button"
                className="btn btn-link p-0 text-primary"
                style={{ textDecoration: "none" }}
                >
                ¿Olvidaste tu contraseña?
            </button>
          </div>

          <button
            type="submit"
            className="btn btn-primary w-100"
            disabled={loading}
          >
            {loading ? "Cargando..." : "Iniciar sesión"}
          </button>

          {error && (
            <div className="alert alert-danger mt-3 text-center">{error}</div>
          )}
        </form>
        <p className="mt-3 text-center">
            ¿No tienes cuenta?{" "}
            <a href="/signup" className="text-primary text-decoration-none">
                Regístrarse
            </a>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
