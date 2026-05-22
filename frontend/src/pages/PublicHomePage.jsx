import React, { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import "./PublicHomePage.css";

const PublicHomePage = () => {
  const { isAuthenticated, user } = useContext(AuthContext);

  return (
    <main className="public-page">
      <nav className="public-nav" aria-label="Navegación principal">
        <Link className="public-brand" to="/">
          ANPA Project
        </Link>
        <div className="public-nav-actions">
          {isAuthenticated ? (
            <Link className="public-nav-link public-nav-primary" to="/dashboard">
              Mi panel
            </Link>
          ) : (
            <>
              <Link className="public-nav-link" to="/login">
                Iniciar sesión
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
          <p className="public-kicker">Asociación de familias</p>
          <h1>Gestión digital para el día a día del ANPA</h1>
          <p>
            Una plataforma para consultar noticias, conocer actividades del centro
            y acceder al área privada cuando sea necesario gestionar inscripciones,
            mensajes o pagos.
          </p>
          <div className="public-hero-actions">
            {isAuthenticated ? (
              <Link className="public-button" to="/dashboard">
                Ir a mi panel
              </Link>
            ) : (
              <>
                <Link className="public-button" to="/signup">
                  Registrarse
                </Link>
                <Link className="public-button public-button-secondary" to="/login">
                  Ya tengo cuenta
                </Link>
              </>
            )}
          </div>
          {user && (
            <p className="public-session-note">
              Sesión iniciada como <strong>{user.userName}</strong>
            </p>
          )}
        </div>
      </section>

      <section className="public-sections" aria-label="Accesos principales">
        <article>
          <h2>Información pública</h2>
          <p>
            Las familias pueden consultar contenidos generales sin iniciar sesión,
            como avisos, noticias publicadas y actividades abiertas.
          </p>
        </article>
        <article>
          <h2>Usuarios registrados</h2>
          <p>
            El registro permite acceder al área privada, gestionar inscripciones
            y consultar comunicaciones personales con el ANPA.
          </p>
        </article>
        <article>
          <h2>Socios del ANPA</h2>
          <p>
            La condición de socio se gestiona de forma independiente al rol de
            usuario y permitirá acceder a contenidos o actividades reservadas.
          </p>
        </article>
      </section>
    </main>
  );
};

export default PublicHomePage;
