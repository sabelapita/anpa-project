import React, { useState, useContext } from "react";
import axios from "api/axios";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "bootstrap/dist/css/bootstrap.min.css";
import "./SignupPage.css";

const SignupPage = () => {
  const [userName, setUserName] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!userName || !firstName || !lastName || !email || !password || !confirmPassword) {
      setError("Por favor, completa todos los campos.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Las contraseñas no coinciden.");
      return;
    }

    try {
    setLoading(true);
    const response = await axios.post("/users/signUp", {
        userName: userName,
        firstName: firstName,
        lastName: lastName,
        password: password,
        email: email,
        role: "USER"
    });

    console.log("Respuesta del backend:", response.data);

    const token = response.data.serviceToken;
    if (token) {
        login(token, true); // login automático
        navigate("/"); // redirige al HomePage
    } else {
        setError("Registro completado, pero sin token.");
    }

    } catch (err) {
    if (err.response) {
        // Errores controlados por backend
        const msg = err.response.data.message || err.response.data.errors?.[0];

        if (msg?.includes("DuplicateInstanceException")) {
        setError("Ese nombre de usuario ya existe.");
        } else if (msg?.includes("IncorrectPasswordException")) {
        setError("La contraseña no cumple los requisitos.");
        } else {
        setError("Error al registrar usuario. Inténtalo de nuevo.");
        }
    } else {
        // Errores de red o inesperados
        setError("No se pudo conectar con el servidor.");
    }
    } finally {
    setLoading(false);
    }

  };

  return (
    <div className="signup-bg d-flex justify-content-center align-items-center vh-100">
      <div className="card signup-card shadow-lg p-4 border-0">
        <h3 className="text-center mb-4 fw-bold text-primary">Crear cuenta</h3>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Usuario</label>
            <input
              type="text"
              className="form-control"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              placeholder="Introduce tu usuario"
            />
          </div>

            <div className="mb-3">
            <label className="form-label">Nombre</label>
            <input
              type="text"
              className="form-control"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="Introduce tu nombre"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Apellidos</label>
            <input
              type="text"
              className="form-control"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Introduce tus apellidos"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Correo electrónico</label>
            <input
              type="email"
              className="form-control"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Introduce tu email"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <input
              type="password"
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Introduce tu contraseña"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Confirmar contraseña</label>
            <input
              type="password"
              className="form-control"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Repite la contraseña"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary w-100"
            disabled={loading}
          >
            {loading ? "Registrando..." : "Registrarse"}
          </button>

          {error && <div className="alert alert-danger mt-3 text-center">{error}</div>}
        </form>

        <p className="mt-3 text-center">
          ¿Ya tienes cuenta?{" "}
          <a href="/login" className="text-primary text-decoration-none">
            Inicia sesión aquí
          </a>
        </p>
      </div>
    </div>
  );
};

export default SignupPage;
