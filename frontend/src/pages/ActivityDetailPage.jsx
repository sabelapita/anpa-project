import React, { useContext, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { findActivityById } from "../api/activityApi";
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
        month: "long",
        year: "numeric",
      }).format(new Date(date))
    : "";

const ActivityDetailPage = () => {
  const { id } = useParams();
  const { isAuthenticated, isAdmin } = useContext(AuthContext);
  const [activity, setActivity] = useState(null);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const enrollment = useMemo(
    () => enrollments.find((item) => item.activityId === Number(id)),
    [enrollments, id]
  );

  useEffect(() => {
    const loadActivity = async () => {
      setLoading(true);
      setError("");

      try {
        const [activityResponse, enrollmentsResponse] = await Promise.all([
          findActivityById(id),
          isAuthenticated && !isAdmin ? findMyEnrollments() : Promise.resolve({ data: [] }),
        ]);

        setActivity(activityResponse.data);
        setEnrollments(enrollmentsResponse.data || []);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudo cargar la actividad."));
      } finally {
        setLoading(false);
      }
    };

    loadActivity();
  }, [id, isAdmin, isAuthenticated]);

  const handleEnrollment = async () => {
    setSubmitting(true);
    setMessage("");
    setError("");

    try {
      const response = await enrollInActivity(activity.id);
      setEnrollments((current) => [...current, response.data]);
      setMessage(
        activity.price > 0
          ? "Inscripcion creada. El pago queda pendiente."
          : "Inscripcion realizada correctamente."
      );
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo realizar la inscripcion."));
    } finally {
      setSubmitting(false);
    }
  };

  const handlePayment = async () => {
    setSubmitting(true);
    setError("");

    try {
      const response = await createCheckoutSession(enrollment.id);
      window.location.assign(response.data.checkoutUrl);
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo iniciar el pago."));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p className="activities-state">Cargando actividad...</p>;
  }

  if (error && !activity) {
    return (
      <main className="activities-page">
        <div className="activities-alert error">{error}</div>
        <Link className="activities-secondary" to="/activities">
          Volver a actividades
        </Link>
      </main>
    );
  }

  const canEnroll = isAuthenticated && !isAdmin && activity.type === "ACTIVITY" && !enrollment;

  return (
    <main className="activities-page">
      <article className="activity-detail">
        <Link className="activity-back" to="/activities">
          Volver a actividades
        </Link>

        <div className="activity-detail-header">
          <div>
            <p className="activities-kicker">
              {typeLabels[activity.type]} · {visibilityLabels[activity.visibility]}
            </p>
            <h1>{activity.title}</h1>
          </div>
          <strong>{formatPrice(activity.price)}</strong>
        </div>

        <p className="activity-detail-description">{activity.description}</p>

        <dl className="activity-detail-data">
          <div>
            <dt>Fecha de inicio</dt>
            <dd>{formatDate(activity.startDate)}</dd>
          </div>
          <div>
            <dt>Fecha de fin</dt>
            <dd>{formatDate(activity.endDate)}</dd>
          </div>
          <div>
            <dt>Inscripcion</dt>
            <dd>{activity.type === "EVENT" ? "No disponible para eventos" : "Disponible"}</dd>
          </div>
        </dl>

        {message && <div className="activities-alert success">{message}</div>}
        {error && <div className="activities-alert error">{error}</div>}

        <div className="activity-detail-actions">
          {!isAuthenticated && activity.type === "ACTIVITY" && (
            <Link className="activities-primary" to="/login">
              Iniciar sesion para inscribirse
            </Link>
          )}
          {isAdmin && activity.type === "ACTIVITY" && (
            <span className="activity-enrolled">Gestion administrativa</span>
          )}
          {canEnroll && (
            <button type="button" onClick={handleEnrollment} disabled={submitting}>
              {submitting ? "Inscribiendo..." : "Inscribirme"}
            </button>
          )}
          {enrollment && activity.price > 0 && !enrollment.paid && (
            <button type="button" onClick={handlePayment} disabled={submitting}>
              {submitting ? "Preparando pago..." : "Pagar inscripcion"}
            </button>
          )}
          {enrollment && (activity.price <= 0 || enrollment.paid) && (
            <span className="activity-enrolled">Ya estas inscrito</span>
          )}
        </div>
      </article>
    </main>
  );
};

export default ActivityDetailPage;
