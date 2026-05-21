import React, { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import "bootstrap/dist/css/bootstrap.min.css";

const HomePage = () => {
  const { user, logout } = useContext(AuthContext);

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
        {user && (
          <p className="text-center text-muted mb-4">
            Usuario: <strong>{user.userName}</strong> · Perfil:{" "}
            <strong>{user.role}</strong>
          </p>
        )}
        <div className="text-center">
          <button className="btn btn-danger px-4" onClick={() => logout()}>
            Cerrar sesión
          </button>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
