import React, { useCallback, useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { findAllMessages, findMyMessages } from "../api/messageApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./MessagesPage.css";

const formatDate = (date) =>
  date
    ? new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(date))
    : "";

const getShortText = (text) => {
  if (!text || text.length <= 150) {
    return text;
  }

  return `${text.slice(0, 150).trim()}...`;
};

const MessagesPage = ({ admin = false }) => {
  const { isAdmin } = useContext(AuthContext);
  const [messages, setMessages] = useState([]);
  const [existMoreItems, setExistMoreItems] = useState(false);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadMessages = useCallback(
    async (nextPage = 0) => {
      setLoading(true);
      setError("");

      try {
        const response =
          admin && isAdmin
            ? await findAllMessages({ page: nextPage, size: 9 })
            : await findMyMessages({ page: nextPage, size: 9 });

        setMessages(response.data.items || []);
        setExistMoreItems(Boolean(response.data.existMoreItems));
        setPage(nextPage);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudieron cargar los mensajes."));
      } finally {
        setLoading(false);
      }
    },
    [admin, isAdmin]
  );

  useEffect(() => {
    loadMessages(0);
  }, [loadMessages]);

  return (
    <main className="messages-page">
      <header className="messages-header">
        <div>
          <p className="messages-kicker">{admin ? "Administracion" : "Area privada"}</p>
          <h1>{admin ? "Mensajes de las familias" : "Mis mensajes"}</h1>
          <p>
            {admin
              ? "Revisa las consultas recibidas y responde desde el panel del ANPA."
              : "Consulta tus mensajes, respuestas del ANPA y avisos internos de nuevas publicaciones."}
          </p>
        </div>

        <div className="messages-actions">
          <Link className="messages-secondary" to="/dashboard">
            Mi panel
          </Link>
          {!admin && !isAdmin && (
            <Link className="messages-primary" to="/messages/new">
              Nuevo mensaje
            </Link>
          )}
        </div>
      </header>

      {error && <div className="messages-alert error">{error}</div>}

      {loading ? (
        <p className="messages-state">Cargando mensajes...</p>
      ) : messages.length === 0 ? (
        <p className="messages-state">Todavia no hay mensajes.</p>
      ) : (
        <section className="messages-table-panel" aria-label="Listado de mensajes">
          <div className="messages-table">
            <table>
              <thead>
                <tr>
                  <th>Estado</th>
                  <th>Asunto</th>
                  {admin && <th>Usuario</th>}
                  <th>Mensaje</th>
                  <th>Fecha</th>
                  <th>Adjuntos</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {messages.map((message) => (
                  <tr key={message.id}>
                    <td>
                      <span className={message.response ? "message-status answered" : "message-status pending"}>
                        {message.response ? "Respondido" : "Pendiente"}
                      </span>
                      {message.subject === "Nueva entrada del blog" && (
                        <span className="message-status notice">Aviso</span>
                      )}
                    </td>
                    <td>
                      <strong>{message.subject}</strong>
                    </td>
                    {admin && (
                      <td>
                        {message.userName}
                        <small>{message.userFullName}</small>
                      </td>
                    )}
                    <td>{getShortText(message.content)}</td>
                    <td>{formatDate(message.sentAt)}</td>
                    <td>{message.attachments?.length || 0}</td>
                    <td>
                      <Link to={admin ? `/admin/messages/${message.id}` : `/messages/${message.id}`}>
                        Abrir
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      <div className="messages-pagination">
        <button type="button" onClick={() => loadMessages(page - 1)} disabled={page === 0}>
          Anterior
        </button>
        <span>Pagina {page + 1}</span>
        <button type="button" onClick={() => loadMessages(page + 1)} disabled={!existMoreItems}>
          Siguiente
        </button>
      </div>
    </main>
  );
};

export default MessagesPage;
