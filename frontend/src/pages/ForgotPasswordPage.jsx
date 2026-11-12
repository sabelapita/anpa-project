import React, { useContext, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { requestPasswordReset } from "../api/authApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./AuthPages.css";

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  const { isAuthenticated } = useContext(AuthContext);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!email.trim()) {
      setError("Introduce tu email.");
      return;
    }

    setError("");
    setSent(false);
    setLoading(true);

    try {
      await requestPasswordReset({ email: email.trim() });
      setSent(true);
    } catch (err) {
      setError(
        getApiErrorMessage(err, "No se ha podido procesar la solicitud.")
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="forgot-password-title">
        <p className="auth-kicker">ANPA Project</p>
        <h1 id="forgot-password-title">Recuperar contrasena</h1>
        <p className="auth-subtitle">
          Escribe el email de tu cuenta y te enviaremos un enlace para crear una nueva contrasena.
        </p>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              maxLength={60}
              autoFocus
            />
          </div>

          {error && (
            <div className="auth-alert" role="alert">
              {error}
            </div>
          )}

          {sent && (
            <div className="auth-alert auth-alert-success" role="status">
              Si existe una cuenta con ese email, recibiras un enlace de recuperacion en unos minutos.
            </div>
          )}

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading ? "Enviando..." : "Enviar enlace"}
          </button>
        </form>

        <p className="auth-switch">
          <Link to="/login">Volver a iniciar sesion</Link>
        </p>
      </section>
    </main>
  );
};

export default ForgotPasswordPage;
