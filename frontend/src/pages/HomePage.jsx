import React, { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "bootstrap/dist/css/bootstrap.min.css";

const getRoleLabel = (role) => {
  if (role === "ADMIN") {
    return "Administrador";
  }

  return "Usuario registrado";
};

const getMembershipLabel = (membershipStatus) => {
  if (membershipStatus === "MEMBER") {
    return "Socio del ANPA";
  }

  return "No socio";
};

const HomePage = () => {
  const { user, isAdmin, isMember, logout } = useContext(AuthContext);

  return (
    <main className="container py-5">
      <section className="card shadow-lg p-4 border-0">
        <div className="d-flex flex-column flex-md-row justify-content-between gap-3 mb-4">
          <div>
            <p className="text-uppercase text-success fw-bold mb-2">
              Area privada
            </p>
            <h1 className="h2 mb-2 text-success fw-bold">
              Panel principal del ANPA
            </h1>
            <p className="text-muted mb-0">
              Desde aqui se agruparan las funcionalidades privadas segun el
              perfil de acceso de cada familia.
            </p>
          </div>
          <div className="d-flex align-items-start gap-2">
            <Link className="btn btn-outline-success" to="/">
              Ver pagina publica
            </Link>
            <Link className="btn btn-success" to="/activities">
              Actividades
            </Link>
            <button className="btn btn-danger" onClick={() => logout()}>
              Cerrar sesion
            </button>
          </div>
        </div>

        {user && (
          <div className="row g-3 mb-4">
            <div className="col-md-4">
              <div className="border rounded p-3 h-100">
                <p className="text-muted mb-1">Usuario</p>
                <strong>{user.userName}</strong>
              </div>
            </div>
            <div className="col-md-4">
              <div className="border rounded p-3 h-100">
                <p className="text-muted mb-1">Rol</p>
                <strong>{getRoleLabel(user.role)}</strong>
              </div>
            </div>
            <div className="col-md-4">
              <div className="border rounded p-3 h-100">
                <p className="text-muted mb-1">Condicion</p>
                <strong>{getMembershipLabel(user.membershipStatus)}</strong>
              </div>
            </div>
          </div>
        )}

        <div className="border-top pt-4">
          <h2 className="h5 mb-3">Politica de acceso prevista</h2>
          <div className="row g-3">
            <div className="col-md-4">
              <div className="bg-light rounded p-3 h-100">
                <strong>Visitantes</strong>
                <p className="text-muted mb-0 mt-2">
                  Podran consultar la portada y los contenidos marcados como
                  publicos sin crear una cuenta.
                </p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="bg-light rounded p-3 h-100">
                <strong>Usuarios registrados</strong>
                <p className="text-muted mb-0 mt-2">
                  Tendran acceso al area privada y a las gestiones generales de
                  la plataforma.
                </p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="bg-light rounded p-3 h-100">
                <strong>Socios y administradores</strong>
                <p className="text-muted mb-0 mt-2">
                  Los socios podran acceder a contenidos reservados. Los
                  administradores gestionaran usuarios y publicaciones.
                </p>
              </div>
            </div>
          </div>
        </div>

        {(isAdmin || isMember) && (
          <p className="text-muted mt-4 mb-0">
            Tu perfil ya tiene permisos ampliados para los proximos modulos.
          </p>
        )}

        {isAdmin && (
          <div className="mt-3">
            <Link className="btn btn-outline-success" to="/admin/activities/new">
              Publicar actividad
            </Link>
          </div>
        )}
      </section>
    </main>
  );
};

export default HomePage;
