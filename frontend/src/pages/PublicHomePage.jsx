import React, { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "./PublicHomePage.css";

const PublicHomePage = () => {
  const { isAuthenticated, user } = useContext(AuthContext);

  return (
    <main className="public-page">
      <nav className="public-nav" aria-label="Navegacion principal">
        <Link className="public-brand" to="/">
          ANPA Project
        </Link>
        <div className="public-nav-actions">
          <Link className="public-nav-link" to="/activities">
            Actividades
          </Link>
          <Link className="public-nav-link" to="/blog">
            Blog
          </Link>
          {isAuthenticated && (
            <Link className="public-nav-link" to="/documents">
              Documentos
            </Link>
          )}
          {isAuthenticated ? (
            <Link className="public-nav-link public-nav-primary" to="/dashboard">
              Mi panel
            </Link>
          ) : (
            <>
              <Link className="public-nav-link" to="/login">
                Iniciar sesion
              </Link>
              <Link className="public-nav-link public-nav-primary" to="/signup">
                Crear cuenta
              </Link>
            </>
          )}
        </div>
      </nav>

      <section className="public-hero">
        <div className="public-hero-content">
          <p className="public-kicker">Asociacion de familias</p>
          <h1>Gestion digital para el dia a dia del ANPA</h1>
          <p>
            Una plataforma para consultar noticias, conocer actividades del centro
            y acceder al area privada cuando sea necesario gestionar inscripciones,
            mensajes o pagos.
          </p>
          {user && (
            <p className="public-session-note">
              Sesion iniciada como <strong>{user.userName}</strong>
            </p>
          )}
        </div>
      </section>
    </main>
  );
};

export default PublicHomePage;
