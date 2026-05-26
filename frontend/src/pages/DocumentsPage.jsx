import React, { useCallback, useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { deleteDocument, downloadDocument, findDocuments } from "../api/documentApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./DocumentsPage.css";

const formatDate = (date) =>
  date
    ? new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
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

const getContentTypeLabel = (contentType) => {
  if (contentType === "application/pdf") {
    return "PDF";
  }

  if (contentType === "image/png") {
    return "PNG";
  }

  if (contentType === "image/jpeg") {
    return "JPG";
  }

  return "Documento";
};

const getShortDescription = (description) => {
  if (!description || description.length <= 130) {
    return description;
  }

  return `${description.slice(0, 130).trim()}...`;
};

const DocumentsPage = () => {
  const { isAdmin } = useContext(AuthContext);
  const [documents, setDocuments] = useState([]);
  const [existMoreItems, setExistMoreItems] = useState(false);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [expandedDescriptions, setExpandedDescriptions] = useState({});
  const [keywords, setKeywords] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadDocuments = useCallback(
    async (nextPage = 0, searchKeywords = keywords) => {
      setLoading(true);
      setError("");

      try {
        const response = await findDocuments({
          page: nextPage,
          size: 9,
          keywords: searchKeywords,
        });

        setDocuments(response.data.items || []);
        setExistMoreItems(Boolean(response.data.existMoreItems));
        setPage(nextPage);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudieron cargar los documentos."));
      } finally {
        setLoading(false);
      }
    },
    [keywords]
  );

  useEffect(() => {
    loadDocuments(0);
  }, [loadDocuments]);

  const handleDownload = async (documentItem) => {
    setDownloadingId(documentItem.id);
    setError("");

    try {
      const response = await downloadDocument(documentItem.id);
      saveBlob(response.data, documentItem.fileName || "documento");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo descargar el documento."));
    } finally {
      setDownloadingId(null);
    }
  };

  const handleDelete = async (documentItem) => {
    const confirmed = window.confirm("Se eliminara el documento. Continuar?");

    if (!confirmed) {
      return;
    }

    setDeletingId(documentItem.id);
    setMessage("");
    setError("");

    try {
      await deleteDocument(documentItem.id);
      setDocuments((current) => current.filter((item) => item.id !== documentItem.id));
      setMessage("Documento eliminado correctamente.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo eliminar el documento."));
    } finally {
      setDeletingId(null);
    }
  };

  const handleSearch = (event) => {
    event.preventDefault();
    loadDocuments(0);
  };

  return (
    <main className="documents-page">
      <header className="documents-header">
        <div>
          <p className="documents-kicker">Documentacion</p>
          <h1>Documentos para las familias</h1>
          <p>
            Consulta la documentacion relevante del ANPA y del centro, como
            autorizaciones, circulares o informacion de inicio de curso.
          </p>
        </div>

        <div className="documents-actions">
          <Link className="documents-secondary" to="/dashboard">
            Mi panel
          </Link>
          {isAdmin && (
            <Link className="documents-primary" to="/admin/documents/new">
              Subir documento
            </Link>
          )}
        </div>
      </header>

      {message && <div className="documents-alert success">{message}</div>}
      {error && <div className="documents-alert error">{error}</div>}

      <form className="documents-search" onSubmit={handleSearch}>
        <label htmlFor="document-search">Buscar por titulo</label>
        <div>
          <input
            id="document-search"
            type="search"
            value={keywords}
            onChange={(event) => setKeywords(event.target.value)}
            placeholder="Titulo del documento"
            maxLength={100}
          />
          <button type="submit">Buscar</button>
          {keywords && (
            <button
              type="button"
              onClick={() => {
                setKeywords("");
                loadDocuments(0, "");
              }}
            >
              Limpiar
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p className="documents-state">Cargando documentos...</p>
      ) : documents.length === 0 ? (
        <p className="documents-state">No hay documentos que coincidan con la busqueda.</p>
      ) : (
        <section className="documents-grid" aria-label="Listado de documentos">
          {documents.map((documentItem) => (
            <article className="document-card" key={documentItem.id}>
              <div>
                <span>{getContentTypeLabel(documentItem.contentType)}</span>
                <h2>{documentItem.title}</h2>
                {documentItem.description && (
                  <div className="document-description">
                    <p>
                      {expandedDescriptions[documentItem.id]
                        ? documentItem.description
                        : getShortDescription(documentItem.description)}
                    </p>
                    {documentItem.description.length > 130 && (
                      <button
                        type="button"
                        onClick={() =>
                          setExpandedDescriptions((current) => ({
                            ...current,
                            [documentItem.id]: !current[documentItem.id],
                          }))
                        }
                      >
                        {expandedDescriptions[documentItem.id] ? "Ver menos" : "Ver mas"}
                      </button>
                    )}
                  </div>
                )}
              </div>

              <dl>
                <div>
                  <dt>Archivo</dt>
                  <dd>{documentItem.fileName}</dd>
                </div>
                <div>
                  <dt>Fecha de subida</dt>
                  <dd>{formatDate(documentItem.uploadedAt)}</dd>
                </div>
              </dl>

              <div className="document-card-actions">
                <button
                  type="button"
                  onClick={() => handleDownload(documentItem)}
                  disabled={downloadingId === documentItem.id}
                >
                  {downloadingId === documentItem.id ? "Descargando..." : "Descargar"}
                </button>
                {isAdmin && (
                  <button
                    type="button"
                    className="documents-danger"
                    onClick={() => handleDelete(documentItem)}
                    disabled={deletingId === documentItem.id}
                  >
                    {deletingId === documentItem.id ? "Eliminando..." : "Eliminar"}
                  </button>
                )}
              </div>
            </article>
          ))}
        </section>
      )}

      <div className="documents-pagination">
        <button type="button" onClick={() => loadDocuments(page - 1)} disabled={page === 0}>
          Anterior
        </button>
        <span>Pagina {page + 1}</span>
        <button type="button" onClick={() => loadDocuments(page + 1)} disabled={!existMoreItems}>
          Siguiente
        </button>
      </div>
    </main>
  );
};

export default DocumentsPage;
