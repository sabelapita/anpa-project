import React, { useContext, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  deletePost,
  downloadPostAttachment,
  findAdminPostById,
  findPublishedPostById,
  getPostAttachmentUrl,
  setPostPublished,
} from "../api/blogApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./BlogPage.css";

const formatDate = (date) =>
  date
    ? new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "long",
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

const isImage = (attachment) => attachment.contentType?.startsWith("image/");

const BlogDetailPage = ({ admin = false }) => {
  const { id } = useParams();
  const { isAdmin } = useContext(AuthContext);
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState(null);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadPost = async () => {
      setLoading(true);
      setError("");

      try {
        const response = admin && isAdmin ? await findAdminPostById(id) : await findPublishedPostById(id);
        setPost(response.data);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudo cargar la entrada."));
      } finally {
        setLoading(false);
      }
    };

    loadPost();
  }, [admin, id, isAdmin]);

  const handleDownload = async (attachment) => {
    setDownloadingId(attachment.id);
    setError("");

    try {
      const response = await downloadPostAttachment(attachment.id);
      saveBlob(response.data, attachment.fileName || "adjunto");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo descargar el adjunto."));
    } finally {
      setDownloadingId(null);
    }
  };

  const handlePublishToggle = async () => {
    setBusy(true);
    setMessage("");
    setError("");

    try {
      const response = await setPostPublished(post.id, !post.published);
      setPost(response.data);
      setMessage(response.data.published ? "Entrada publicada correctamente." : "Entrada despublicada.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo cambiar el estado de publicacion."));
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async () => {
    const confirmed = window.confirm("Se eliminara la entrada del blog. Continuar?");

    if (!confirmed) {
      return;
    }

    setBusy(true);
    setError("");

    try {
      await deletePost(post.id);
      navigate("/blog", { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo eliminar la entrada."));
    } finally {
      setBusy(false);
    }
  };

  if (loading) {
    return <p className="blog-state">Cargando entrada...</p>;
  }

  if (error && !post) {
    return (
      <main className="blog-page">
        <div className="blog-alert error">{error}</div>
        <Link className="blog-secondary" to="/blog">
          Volver al blog
        </Link>
      </main>
    );
  }

  return (
    <main className="blog-page">
      <article className="blog-detail">
        <Link className="blog-back" to="/blog">
          Volver al blog
        </Link>

        <div className="blog-detail-header">
          <div>
            <p className="blog-kicker">{post.category || "General"}</p>
            <h1>{post.title}</h1>
            <p>{formatDate(post.createdAt)}</p>
          </div>
          {isAdmin && (
            <span className={post.published ? "blog-status published" : "blog-status draft"}>
              {post.published ? "Publicada" : "No publicada"}
            </span>
          )}
        </div>

        {message && <div className="blog-alert success">{message}</div>}
        {error && <div className="blog-alert error">{error}</div>}

        <div className="blog-detail-content">
          {post.content?.split("\n").map((paragraph, index) => (
            <p key={`${post.id}-${index}`}>{paragraph}</p>
          ))}
        </div>

        {post.attachments?.length > 0 && (
          <section className="blog-attachments">
            <h2>Adjuntos</h2>
            <div className="blog-attachment-grid">
              {post.attachments.map((attachment) => (
                <article className="blog-attachment" key={attachment.id}>
                  {isImage(attachment) && post.published && (
                    <img src={getPostAttachmentUrl(attachment.id)} alt={attachment.fileName} />
                  )}
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

        {isAdmin && (
          <div className="blog-detail-actions">
            <Link className="blog-secondary" to={`/admin/blog/${post.id}/edit`}>
              Editar
            </Link>
            <button type="button" onClick={handlePublishToggle} disabled={busy}>
              {post.published ? "Despublicar" : "Publicar"}
            </button>
            <button type="button" className="blog-danger" onClick={handleDelete} disabled={busy}>
              Eliminar
            </button>
          </div>
        )}
      </article>
    </main>
  );
};

export default BlogDetailPage;
