import React, { useContext, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { signUpUser } from "../api/authApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import { hasMaxLength, isValidEmail } from "../utils/validators";
import "./AuthPages.css";

const initialFormData = {
  userName: "",
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  confirmPassword: "",
  remember: false,
};

const SignupPage = () => {
  const [formData, setFormData] = useState(initialFormData);
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
    const requiredFields = [
      formData.userName,
      formData.firstName,
      formData.lastName,
      formData.email,
      formData.password,
      formData.confirmPassword,
    ];

    if (requiredFields.some((value) => !value.trim())) {
      return "Completa todos los campos para crear la cuenta.";
    }

    if (!isValidEmail(formData.email.trim())) {
      return "Introduce un correo electrónico válido.";
    }

    if (formData.password !== formData.confirmPassword) {
      return "Las contraseñas no coinciden.";
    }

    if (
      !hasMaxLength(formData.userName, 60) ||
      !hasMaxLength(formData.firstName, 60) ||
      !hasMaxLength(formData.lastName, 60) ||
      !hasMaxLength(formData.email, 60) ||
      formData.password.length > 60
    ) {
      return "Los campos no pueden superar los 60 caracteres.";
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
      const response = await signUpUser({
        userName: formData.userName.trim(),
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim(),
        password: formData.password,
      });

      login(response.data, formData.remember);
      navigate("/dashboard", { replace: true });
    } catch (err) {
      const message = getApiErrorMessage(
        err,
        "No se pudo crear la cuenta. Revisa los datos e inténtalo de nuevo."
      );

      setError(
        message.includes("Ya existe")
          ? "Ya existe una cuenta con ese nombre de usuario."
          : message
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card auth-card-wide" aria-labelledby="signup-title">
        <p className="auth-kicker">ANPA Project</p>
        <h1 id="signup-title">Crear cuenta</h1>
        <p className="auth-subtitle">
          Regístrate para inscribirte en actividades y consultar tus gestiones.
        </p>

        <form onSubmit={handleSubmit} noValidate>
          <div className="auth-grid">
            <div className="form-field">
              <label htmlFor="signup-userName">Usuario</label>
              <input
                id="signup-userName"
                type="text"
                value={formData.userName}
                onChange={(event) => updateField("userName", event.target.value)}
                autoComplete="username"
                maxLength={60}
                autoFocus
              />
            </div>

            <div className="form-field">
              <label htmlFor="signup-email">Correo electrónico</label>
              <input
                id="signup-email"
                type="email"
                value={formData.email}
                onChange={(event) => updateField("email", event.target.value)}
                autoComplete="email"
                maxLength={60}
              />
            </div>

            <div className="form-field">
              <label htmlFor="firstName">Nombre</label>
              <input
                id="firstName"
                type="text"
                value={formData.firstName}
                onChange={(event) => updateField("firstName", event.target.value)}
                autoComplete="given-name"
                maxLength={60}
              />
            </div>

            <div className="form-field">
              <label htmlFor="lastName">Apellidos</label>
              <input
                id="lastName"
                type="text"
                value={formData.lastName}
                onChange={(event) => updateField("lastName", event.target.value)}
                autoComplete="family-name"
                maxLength={60}
              />
            </div>

            <div className="form-field">
              <label htmlFor="signup-password">Contraseña</label>
              <input
                id="signup-password"
                type="password"
                value={formData.password}
                onChange={(event) => updateField("password", event.target.value)}
                autoComplete="new-password"
                maxLength={60}
              />
            </div>

            <div className="form-field">
              <label htmlFor="confirmPassword">Confirmar contraseña</label>
              <input
                id="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={(event) =>
                  updateField("confirmPassword", event.target.value)
                }
                autoComplete="new-password"
                maxLength={60}
              />
            </div>
          </div>

          <label className="auth-check" htmlFor="signup-remember">
            <input
              id="signup-remember"
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
            {loading ? "Creando cuenta..." : "Registrarse"}
          </button>
        </form>

        <p className="auth-switch">
          ¿Ya tienes cuenta? <Link to="/login">Iniciar sesión</Link>
        </p>
      </section>
    </main>
  );
};

export default SignupPage;
