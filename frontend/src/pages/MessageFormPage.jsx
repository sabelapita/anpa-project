import React, { useContext, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { createMessage } from "../api/messageApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./MessagesPage.css";

const MessageFormPage = () => {
  const { isAdmin } = useContext(AuthContext);
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    subject: "",
    content: "",
    files: [],
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  if (isAdmin) {
    return <Navigate to="/admin/messages" replace />;
  }

  const updateField = (field, value) => {
    setFormData((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!formData.subject.trim() || !formData.content.trim()) {
      setError("Completa asunto y mensaje.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const response = await createMessage({
        subject: formData.subject.trim(),
        content: formData.content.trim(),
        files: formData.files,
      });

      navigate(`/messages/${response.data.id}`, { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo enviar el mensaje."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="messages-page">
      <section className="message-form-panel">
        <Link className="message-back" to="/messages">
          Volver a mensajes
        </Link>

        <p className="messages-kicker">Area privada</p>
        <h1>Nuevo mensaje</h1>

        {error && <div className="messages-alert error">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <label>
            Asunto
            <input
              type="text"
              value={formData.subject}
              onChange={(event) => updateField("subject", event.target.value)}
              maxLength={100}
            />
          </label>

          <label>
            Mensaje
            <textarea
              value={formData.content}
              onChange={(event) => updateField("content", event.target.value)}
              rows={8}
              maxLength={5000}
            />
            <span className="messages-help">{formData.content.length}/5000 caracteres</span>
          </label>

          <label>
            Adjuntos
            <input
              type="file"
              accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg"
              multiple
              onChange={(event) => updateField("files", Array.from(event.target.files || []))}
            />
            <span className="messages-help">
              Formatos permitidos: PDF, PNG, JPG. Puedes seleccionar varios archivos.
            </span>
          </label>

          <div className="messages-actions">
            <button className="messages-primary" type="submit" disabled={submitting}>
              {submitting ? "Enviando..." : "Enviar mensaje"}
            </button>
            <Link className="messages-secondary" to="/messages">
              Cancelar
            </Link>
          </div>
        </form>
      </section>
    </main>
  );
};

export default MessageFormPage;
