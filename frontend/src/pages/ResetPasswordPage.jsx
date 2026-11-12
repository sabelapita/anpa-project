import React, { useContext, useState } from "react";
import { Link, Navigate, useSearchParams } from "react-router-dom";
import { resetPassword } from "../api/authApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./AuthPages.css";

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";

  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [completed, setCompleted] = useState(false);
  const [loading, setLoading] = useState(false);

  const { isAuthenticated } = useContext(AuthContext);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const updateField = (field, value) => {
    setFormData((current) => ({ ...current, [field]: value }));
  };

  const validateForm = () => {
    if (!token) {
      return "El enlace de recuperacion no es valido.";
    }

    if (!formData.newPassword) {
      return "Introduce la nueva contrasena.";
    }

    if (formData.newPassword.length > 60) {
      return "La contrasena no puede superar los 60 caracteres.";
    }

    if (formData.newPassword !== formData.confirmPassword) {
      return "Las contrasenas no coinciden.";
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
      await resetPassword({ token, newPassword: formData.newPassword });
      setCompleted(true);
      setFormData({ newPassword: "", confirmPassword: "" });
    } catch (err) {
      setError(
        getApiErrorMessage(err, "No se ha podido cambiar la contrasena.")
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="reset-password-title">
        <p className="auth-kicker">ANPA Project</p>
        <h1 id="reset-password-title">Nueva contrasena</h1>
        <p className="auth-subtitle">
          Elige una nueva contrasena para volver a acceder a tu cuenta.
        </p>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="newPassword">Nueva contrasena</label>
            <input
              id="newPassword"
              type="password"
              value={formData.newPassword}
              onChange={(event) => updateField("newPassword", event.target.value)}
              autoComplete="new-password"
              maxLength={60}
              autoFocus
            />
          </div>

          <div className="form-field">
            <label htmlFor="confirmPassword">Repetir contrasena</label>
            <input
              id="confirmPassword"
              type="password"
              value={formData.confirmPassword}
              onChange={(event) => updateField("confirmPassword", event.target.value)}
              autoComplete="new-password"
              maxLength={60}
            />
          </div>

          {error && (
            <div className="auth-alert" role="alert">
              {error}
            </div>
          )}

          {completed && (
            <div className="auth-alert auth-alert-success" role="status">
              Tu contrasena se ha actualizado correctamente.
            </div>
          )}

          <button className="auth-submit" type="submit" disabled={loading || completed}>
            {loading ? "Guardando..." : "Guardar contrasena"}
          </button>
        </form>

        <p className="auth-switch">
          <Link to="/login">Iniciar sesion</Link>
        </p>
      </section>
    </main>
  );
};

export default ResetPasswordPage;
