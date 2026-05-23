import React, { useContext, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { createActivity } from "../api/activityApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./ActivitiesPage.css";

const initialFormData = {
  title: "",
  description: "",
  startDate: "",
  endDate: "",
  price: "0",
  type: "ACTIVITY",
  visibility: "PUBLIC",
};

const ActivityFormPage = () => {
  const { isAdmin } = useContext(AuthContext);
  const [formData, setFormData] = useState(initialFormData);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  if (!isAdmin) {
    return <Navigate to="/activities" replace />;
  }

  const updateField = (field, value) => {
    setFormData((current) => ({ ...current, [field]: value }));
  };

  const validateForm = () => {
    if (!formData.title.trim() || !formData.description.trim()) {
      return "Completa titulo y descripcion.";
    }

    if (!formData.startDate || !formData.endDate) {
      return "Indica las fechas de inicio y fin.";
    }

    if (formData.endDate < formData.startDate) {
      return "La fecha de fin no puede ser anterior a la fecha de inicio.";
    }

    if (Number(formData.price) < 0) {
      return "El precio no puede ser negativo.";
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
      const response = await createActivity({
        title: formData.title.trim(),
        description: formData.description.trim(),
        startDate: formData.startDate,
        endDate: formData.endDate,
        price: Number(formData.price),
        type: formData.type,
        visibility: formData.visibility,
      });

      navigate(`/activities/${response.data.id}`, { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo publicar la actividad."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="activities-page">
      <section className="activity-form-panel">
        <Link className="activity-back" to="/activities">
          Volver a actividades
        </Link>
        <p className="activities-kicker">Administracion</p>
        <h1>Publicar actividad o evento</h1>

        <form onSubmit={handleSubmit} noValidate>
          <div className="activity-form-grid">
            <label>
              Titulo
              <input
                type="text"
                value={formData.title}
                onChange={(event) => updateField("title", event.target.value)}
                maxLength={100}
              />
            </label>

            <label>
              Tipo
              <select
                value={formData.type}
                onChange={(event) => updateField("type", event.target.value)}
              >
                <option value="ACTIVITY">Actividad con inscripcion</option>
                <option value="EVENT">Evento informativo</option>
              </select>
            </label>

            <label>
              Fecha de inicio
              <input
                type="date"
                value={formData.startDate}
                onChange={(event) => updateField("startDate", event.target.value)}
              />
            </label>

            <label>
              Fecha de fin
              <input
                type="date"
                value={formData.endDate}
                onChange={(event) => updateField("endDate", event.target.value)}
              />
            </label>

            <label>
              Precio
              <input
                type="number"
                min="0"
                step="0.01"
                value={formData.price}
                onChange={(event) => updateField("price", event.target.value)}
              />
            </label>

            <label>
              Visibilidad
              <select
                value={formData.visibility}
                onChange={(event) => updateField("visibility", event.target.value)}
              >
                <option value="PUBLIC">Publica</option>
                <option value="REGISTERED">Solo usuarios registrados</option>
                <option value="MEMBERS_ONLY">Solo socios del ANPA</option>
              </select>
            </label>
          </div>

          <label className="activity-form-description">
            Descripcion
            <textarea
              value={formData.description}
              onChange={(event) => updateField("description", event.target.value)}
              maxLength={500}
              rows={6}
            />
          </label>

          {error && <div className="activities-alert error">{error}</div>}

          <button className="activities-primary" type="submit" disabled={loading}>
            {loading ? "Publicando..." : "Publicar"}
          </button>
        </form>
      </section>
    </main>
  );
};

export default ActivityFormPage;
