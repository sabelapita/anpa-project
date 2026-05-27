import React, { useContext, useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { createPost, findAdminPostById, updatePost } from "../api/blogApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./BlogPage.css";

const initialFormData = {
  title: "",
  category: "",
  content: "",
  published: true,
  files: [],
};

const BlogFormPage = () => {
  const { isAdmin } = useContext(AuthContext);
  const { id } = useParams();
  const navigate = useNavigate();
  const editing = Boolean(id);
  const [formData, setFormData] = useState(initialFormData);
  const [existingAttachments, setExistingAttachments] = useState([]);
  const [attachmentsToDelete, setAttachmentsToDelete] = useState([]);
  const [loadingPost, setLoadingPost] = useState(editing);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!editing || !isAdmin) {
      return;
    }

    setLoadingPost(true);
    findAdminPostById(id)
      .then((response) => {
        const post = response.data;

        setFormData({
          title: post.title || "",
          category: post.category || "",
          content: post.content || "",
          published: Boolean(post.published),
          files: [],
        });
        setExistingAttachments(post.attachments || []);
        setAttachmentsToDelete([]);
      })
      .catch((err) => setError(getApiErrorMessage(err, "No se pudo cargar la entrada.")))
      .finally(() => setLoadingPost(false));
  }, [editing, id, isAdmin]);

  if (!isAdmin) {
    return <Navigate to="/blog" replace />;
  }

  const updateField = (field, value) => {
    setFormData((current) => ({ ...current, [field]: value }));
  };

  const toggleAttachmentToDelete = (attachmentId) => {
    setAttachmentsToDelete((current) =>
      current.includes(attachmentId)
        ? current.filter((idToDelete) => idToDelete !== attachmentId)
        : [...current, attachmentId]
    );
  };

  const validateForm = () => {
    if (!formData.title.trim() || !formData.content.trim()) {
      return "Completa titulo y contenido.";
    }

    if (formData.category.length > 50) {
      return "La categoria no puede superar los 50 caracteres.";
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

    setSubmitting(true);
    setError("");

    try {
      const payload = {
        title: formData.title.trim(),
        category: formData.category.trim(),
        content: formData.content.trim(),
        published: formData.published,
        files: formData.files,
        attachmentsToDelete,
      };

      const response = editing ? await updatePost(id, payload) : await createPost(payload);
      navigate(`/admin/blog/${response.data.id}`, { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo guardar la entrada."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="blog-page">
      <section className="blog-form-panel">
        <Link className="blog-back" to="/blog">
          Volver al blog
        </Link>
        <p className="blog-kicker">Administracion</p>
        <h1>{editing ? "Editar entrada" : "Nueva entrada del blog"}</h1>

        {loadingPost ? (
          <p className="blog-state">Cargando entrada...</p>
        ) : (
          <form onSubmit={handleSubmit} noValidate>
            <div className="blog-form-grid">
              <label>
                Titulo
                <input
                  type="text"
                  value={formData.title}
                  onChange={(event) => updateField("title", event.target.value)}
                  maxLength={200}
                />
              </label>

              <label>
                Categoria
                <input
                  type="text"
                  value={formData.category}
                  onChange={(event) => updateField("category", event.target.value)}
                  maxLength={50}
                  placeholder="Excursiones, noticias, comedor..."
                />
              </label>
            </div>

            <label className="blog-checkbox">
              <input
                type="checkbox"
                checked={formData.published}
                onChange={(event) => updateField("published", event.target.checked)}
              />
              Publicar al guardar
            </label>

            <label>
              Contenido
              <textarea
                value={formData.content}
                onChange={(event) => updateField("content", event.target.value)}
                rows={10}
                maxLength={5000}
              />
              <span className="blog-help">{formData.content.length}/5000 caracteres</span>
            </label>

            {existingAttachments.length > 0 && (
              <div className="blog-existing-files">
                <strong>Adjuntos actuales</strong>
                <ul>
                  {existingAttachments.map((attachment) => (
                    <li key={attachment.id}>
                      <label className="blog-file-delete">
                        <input
                          type="checkbox"
                          checked={attachmentsToDelete.includes(attachment.id)}
                          onChange={() => toggleAttachmentToDelete(attachment.id)}
                        />
                        <span>{attachment.fileName}</span>
                        <small>Eliminar al guardar</small>
                      </label>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <label>
              Nuevos adjuntos
              <input
                type="file"
                accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg"
                multiple
                onChange={(event) =>
                  updateField("files", Array.from(event.target.files || []))
                }
              />
              <span className="blog-help">
                Formatos permitidos: PDF, PNG, JPG. Puedes seleccionar varios archivos.
              </span>
              {formData.files.length > 0 && (
                <span className="blog-help">{formData.files.length} archivo(s) seleccionado(s)</span>
              )}
            </label>

            {error && <div className="blog-alert error">{error}</div>}

            <div className="blog-actions">
              <button className="blog-primary" type="submit" disabled={submitting}>
                {submitting ? "Guardando..." : "Guardar entrada"}
              </button>
              <Link className="blog-secondary" to="/blog">
                Cancelar
              </Link>
            </div>
          </form>
        )}
      </section>
    </main>
  );
};

export default BlogFormPage;
