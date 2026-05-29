import React, { useContext, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  downloadMessageAttachment,
  findMessageById,
  replyMessage,
  respondMessage,
} from "../api/messageApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./MessagesPage.css";

const formatDate = (date) =>
  date
    ? new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "long",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(date))
    : "";

const saveBlob = (blob, fileName) => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

const MessageDetailPage = ({ admin = false }) => {
  const { id } = useParams();
  const { isAdmin } = useContext(AuthContext);
  const [message, setMessage] = useState(null);
  const [response, setResponse] = useState("");
  const [userReply, setUserReply] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [downloadingId, setDownloadingId] = useState(null);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");

    findMessageById(id)
      .then((result) => {
        setMessage(result.data);
        setResponse("");
        setUserReply("");
      })
      .catch((err) => setError(getApiErrorMessage(err, "No se pudo cargar el mensaje.")))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDownload = async (attachment) => {
    setDownloadingId(attachment.id);
    setError("");

    try {
      const result = await downloadMessageAttachment(attachment.id);
      saveBlob(result.data, attachment.fileName || "adjunto");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo descargar el adjunto."));
    } finally {
      setDownloadingId(null);
    }
  };

  const handleRespond = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setSuccess("");
    setError("");

    try {
      const result = await respondMessage(message.id, response.trim());
      setMessage(result.data);
      setResponse("");
      setSuccess("Respuesta guardada correctamente.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo guardar la respuesta."));
    } finally {
      setSubmitting(false);
    }
  };

  const handleUserReply = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setSuccess("");
    setError("");

    try {
      const result = await replyMessage(message.id, userReply.trim());
      setMessage(result.data);
      setUserReply("");
      setSuccess("Respuesta enviada correctamente.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo enviar la respuesta."));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p className="messages-state">Cargando mensaje...</p>;
  }

  if (error && !message) {
    return (
      <main className="messages-page">
        <div className="messages-alert error">{error}</div>
        <Link className="messages-secondary" to={admin ? "/admin/messages" : "/messages"}>
          Volver a mensajes
        </Link>
      </main>
    );
  }

  return (
    <main className="messages-page">
      <article className="message-detail">
        <Link className="message-back" to={admin ? "/admin/messages" : "/messages"}>
          Volver a mensajes
        </Link>

        <div className="message-detail-header">
          <div>
            <p className="messages-kicker">{message.response ? "Respondido" : "Pendiente"}</p>
            <h1>{message.subject}</h1>
            <p>{formatDate(message.sentAt)}</p>
            {admin && <p>{message.userName} · {message.userFullName}</p>}
          </div>
        </div>

        {success && <div className="messages-alert success">{success}</div>}
        {error && <div className="messages-alert error">{error}</div>}

        <section className="message-body">
          <h2>Mensaje</h2>
          {message.content?.split("\n").map((paragraph, index) => (
            <p key={`${message.id}-content-${index}`}>{paragraph}</p>
          ))}
        </section>

        {message.attachments?.length > 0 && (
          <section className="message-attachments">
            <h2>Adjuntos</h2>
            <div className="message-attachment-grid">
              {message.attachments.map((attachment) => (
                <article className="message-attachment" key={attachment.id}>
                  <strong>{attachment.fileName}</strong>
                  <span>{attachment.contentType === "application/pdf" ? "PDF" : "Imagen"}</span>
                  <button
                    type="button"
                    onClick={() => handleDownload(attachment)}
                    disabled={downloadingId === attachment.id}
                  >
                    {downloadingId === attachment.id ? "Descargando..." : "Descargar"}
                  </button>
                </article>
              ))}
            </div>
          </section>
        )}

        {message.replies?.length > 0 && (
          <section className="message-thread">
            <h2>Hilo de respuestas</h2>
            {message.replies.map((reply) => (
              <article
                className={reply.adminReply ? "message-thread-item admin" : "message-thread-item user"}
                key={reply.id}
              >
                <strong>{reply.adminReply ? "ANPA" : "Usuario"}</strong>
                {reply.content.split("\n").map((paragraph, index) => (
                  <p key={`${reply.id}-${index}`}>{paragraph}</p>
                ))}
                <small>{formatDate(reply.sentAt)}</small>
              </article>
            ))}
          </section>
        )}

        {admin && isAdmin && (
          <form className="message-response-form" onSubmit={handleRespond}>
            <label>
              Nueva respuesta
              <textarea
                value={response}
                onChange={(event) => setResponse(event.target.value)}
                maxLength={5000}
                rows={7}
                required
              />
              <span className="messages-help">{response.length}/5000 caracteres</span>
            </label>
            <button className="messages-primary" type="submit" disabled={submitting}>
              {submitting ? "Guardando..." : "Guardar respuesta"}
            </button>
          </form>
        )}

        {!admin && !isAdmin && message.response && (
          <form className="message-response-form" onSubmit={handleUserReply}>
            <label>
              Contestar en este hilo
              <textarea
                value={userReply}
                onChange={(event) => setUserReply(event.target.value)}
                maxLength={5000}
                rows={6}
                required
              />
              <span className="messages-help">{userReply.length}/5000 caracteres</span>
            </label>
            <button className="messages-primary" type="submit" disabled={submitting}>
              {submitting ? "Enviando..." : "Enviar respuesta"}
            </button>
          </form>
        )}
      </article>
    </main>
  );
};

export default MessageDetailPage;
