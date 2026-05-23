import React, { useCallback, useContext, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { findActivities } from "../api/activityApi";
import { enrollInActivity, findMyEnrollments } from "../api/enrollmentApi";
import { createCheckoutSession } from "../api/paymentApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./ActivitiesPage.css";

const visibilityLabels = {
  PUBLIC: "Publica",
  REGISTERED: "Usuarios registrados",
  MEMBERS_ONLY: "Socios del ANPA",
};

const typeLabels = {
  ACTIVITY: "Actividad",
  EVENT: "Evento",
};

const formatPrice = (price) => {
  if (!price || price <= 0) {
    return "Gratuita";
  }

  return new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency: "EUR",
  }).format(price);
};

const formatDate = (date) =>
  date
    ? new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }).format(new Date(date))
    : "";

const ActivitiesPage = () => {
  const { isAuthenticated, isAdmin } = useContext(AuthContext);
  const [activities, setActivities] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [existMoreItems, setExistMoreItems] = useState(false);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [enrollingId, setEnrollingId] = useState(null);

  const enrolledByActivity = useMemo(
    () =>
      enrollments.reduce((result, enrollment) => {
        result[enrollment.activityId] = enrollment;
        return result;
      }, {}),
    [enrollments]
  );

  const loadActivities = useCallback(
    async (nextPage = 0) => {
      setLoading(true);
      setError("");

      try {
        const [activitiesResponse, enrollmentsResponse] = await Promise.all([
          findActivities({ page: nextPage, size: 9 }),
          isAuthenticated && !isAdmin ? findMyEnrollments() : Promise.resolve({ data: [] }),
        ]);

        setActivities(activitiesResponse.data.items || []);
        setExistMoreItems(Boolean(activitiesResponse.data.existMoreItems));
        setEnrollments(enrollmentsResponse.data || []);
        setPage(nextPage);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudieron cargar las actividades."));
      } finally {
        setLoading(false);
      }
    },
    [isAdmin, isAuthenticated]
  );

  useEffect(() => {
    loadActivities(0);
  }, [loadActivities]);

  const handleEnrollment = async (activity) => {
    setEnrollingId(activity.id);
    setMessage("");
    setError("");

    try {
      const response = await enrollInActivity(activity.id);
      const enrollment = response.data;
      setEnrollments((current) => [...current, enrollment]);

      if (activity.price > 0) {
        setMessage("Inscripcion creada. Puedes completar el pago desde esta pantalla.");
        return;
      }

      setMessage("Inscripcion realizada correctamente.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo realizar la inscripcion."));
    } finally {
      setEnrollingId(null);
    }
  };

  const handlePayment = async (enrollmentId) => {
    setMessage("");
    setError("");

    try {
      const response = await createCheckoutSession(enrollmentId);
      window.location.assign(response.data.checkoutUrl);
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo iniciar el pago."));
    }
  };

  return (
    <main className="activities-page">
      <header className="activities-header">
        <div>
          <p className="activities-kicker">Actividades y eventos</p>
          <h1>Programacion del ANPA</h1>
          <p>
            Consulta las propuestas disponibles. La visibilidad de cada elemento
            depende de si navegas como visitante, usuario registrado o socio.
          </p>
        </div>

        <div className="activities-actions">
          <Link className="activities-secondary" to="/">
            Portada
          </Link>
          {isAdmin && (
            <Link className="activities-primary" to="/admin/activities/new">
              Publicar actividad
            </Link>
          )}
        </div>
      </header>

      {message && <div className="activities-alert success">{message}</div>}
      {error && <div className="activities-alert error">{error}</div>}

      {loading ? (
        <p className="activities-state">Cargando actividades...</p>
      ) : activities.length === 0 ? (
        <p className="activities-state">No hay actividades visibles para tu perfil.</p>
      ) : (
        <section className="activities-grid" aria-label="Listado de actividades">
          {activities.map((activity) => {
            const enrollment = enrolledByActivity[activity.id];
            const canEnroll =
              isAuthenticated && !isAdmin && activity.type === "ACTIVITY" && !enrollment;

            return (
              <article className="activity-card" key={activity.id}>
                <div className="activity-card-header">
                  <span>{typeLabels[activity.type]}</span>
                  <span>{visibilityLabels[activity.visibility]}</span>
                </div>
                <h2>{activity.title}</h2>
                <p>{activity.description}</p>
                <dl>
                  <div>
                    <dt>Fechas</dt>
                    <dd>
                      {formatDate(activity.startDate)} - {formatDate(activity.endDate)}
                    </dd>
                  </div>
                  <div>
                    <dt>Precio</dt>
                    <dd>{formatPrice(activity.price)}</dd>
                  </div>
                </dl>
                <div className="activity-card-actions">
                  <Link to={`/activities/${activity.id}`}>Ver detalle</Link>
                  {!isAuthenticated && activity.type === "ACTIVITY" && (
                    <Link to="/login">Iniciar sesion para inscribirse</Link>
                  )}
                  {isAdmin && activity.type === "ACTIVITY" && (
                    <span className="activity-enrolled">Gestion administrativa</span>
                  )}
                  {canEnroll && (
                    <button
                      type="button"
                      onClick={() => handleEnrollment(activity)}
                      disabled={enrollingId === activity.id}
                    >
                      {enrollingId === activity.id ? "Inscribiendo..." : "Inscribirme"}
                    </button>
                  )}
                  {enrollment && activity.price > 0 && !enrollment.paid && (
                    <button type="button" onClick={() => handlePayment(enrollment.id)}>
                      Pagar
                    </button>
                  )}
                  {enrollment && (activity.price <= 0 || enrollment.paid) && (
                    <span className="activity-enrolled">Inscrito</span>
                  )}
                </div>
              </article>
            );
          })}
        </section>
      )}

      <div className="activities-pagination">
        <button type="button" onClick={() => loadActivities(page - 1)} disabled={page === 0}>
          Anterior
        </button>
        <span>Pagina {page + 1}</span>
        <button type="button" onClick={() => loadActivities(page + 1)} disabled={!existMoreItems}>
          Siguiente
        </button>
      </div>
    </main>
  );
};

export default ActivitiesPage;
