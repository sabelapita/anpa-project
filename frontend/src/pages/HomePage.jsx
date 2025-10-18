import React, { useContext, useEffect } from "react";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const HomePage = () => {
  const { token, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) navigate("/login");
  }, [token, navigate]);

  return (
    <div className="container py-5">
      <div className="card shadow-lg p-4 border-0">
        <h2 className="text-center mb-4 text-success fw-bold">
          Panel principal del ANPA
        </h2>
        <p className="text-center mb-3">
          Has iniciado sesión correctamente. Desde aquí podrás acceder a las
          secciones privadas.
        </p>
        <div className="text-center">
          <button className="btn btn-danger px-4" onClick={logout}>
            Cerrar sesión
          </button>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
