import React, { useContext, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { loginUser } from "../api/authApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./AuthPages.css";

const LoginPage = () => {
  const [formData, setFormData] = useState({
    userName: "",
    password: "",
    remember: false,
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const { isAuthenticated, login } = useContext(AuthContext);
  const navigate = useNavigate();

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const updateField = (field, value) => {
    setFormData((current) => ({ ...current, [field]: value }));
  };

  const validateForm = () => {
    if (!formData.userName.trim() || !formData.password) {
      return "Introduce tu usuario y contraseña.";
    }

    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setError("");
    setLoading(true);

    try {
      const response = await loginUser({
        userName: formData.userName.trim(),
        password: formData.password,
      });

      login(response.data, formData.remember);
      navigate("/dashboard", { replace: true });
    } catch (err) {
      setError(
        getApiErrorMessage(err, "Usuario o contraseña incorrectos.")
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="login-title">
        <p className="auth-kicker">ANPA Project</p>
        <h1 id="login-title">Iniciar sesión</h1>
        <p className="auth-subtitle">
          Accede al área privada para gestionar actividades, mensajes y pagos.
        </p>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="userName">Usuario</label>
            <input
              id="userName"
              type="text"
              value={formData.userName}
              onChange={(event) => updateField("userName", event.target.value)}
              autoComplete="username"
              maxLength={60}
              autoFocus
            />
          </div>

          <div className="form-field">
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              value={formData.password}
              onChange={(event) => updateField("password", event.target.value)}
              autoComplete="current-password"
              maxLength={60}
            />
          </div>

          <label className="auth-check" htmlFor="remember">
            <input
              id="remember"
              type="checkbox"
              checked={formData.remember}
              onChange={(event) => updateField("remember", event.target.checked)}
            />
            <span>Mantener la sesión iniciada</span>
          </label>

          {error && (
            <div className="auth-alert" role="alert">
              {error}
            </div>
          )}

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading ? "Comprobando..." : "Entrar"}
          </button>
        </form>

        <p className="auth-switch">
          ¿No tienes cuenta? <Link to="/signup">Crear una cuenta</Link>
        </p>
        <p className="auth-switch auth-switch-compact">
          <Link to="/forgot-password">He olvidado mi contrasena</Link>
        </p>
      </section>
    </main>
  );
};

export default LoginPage;
