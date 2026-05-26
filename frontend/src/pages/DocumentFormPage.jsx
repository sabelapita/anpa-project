import React, { useContext, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { uploadDocument } from "../api/documentApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./DocumentsPage.css";

const DocumentFormPage = () => {
  const { isAdmin } = useContext(AuthContext);
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: "",
    description: "",
    files: [],
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  if (!isAdmin) {
    return <Navigate to="/documents" replace />;
  }

  const handleChange = (event) => {
    const { name, value, files } = event.target;

    setForm((current) => ({
      ...current,
      [name]: files ? Array.from(files) : value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError("");

    if (form.files.length === 0) {
      setError("Selecciona al menos un archivo para subir.");
      setSubmitting(false);
      return;
    }

    try {
      await uploadDocument(form);
      navigate("/documents", { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo subir el documento."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="documents-page">
      <section className="document-form-panel">
        <Link className="document-back" to="/documents">
          Volver a documentos
        </Link>

        <p className="documents-kicker">Administracion</p>
        <h1>Subir documento</h1>

        {error && <div className="documents-alert error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label>
            Titulo
            <input
              name="title"
              type="text"
              value={form.title}
              onChange={handleChange}
              maxLength={100}
              required
            />
          </label>

          <label>
            Descripcion
            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              rows={4}
              maxLength={250}
            />
            <span className="document-help">{form.description.length}/250 caracteres</span>
          </label>

          <label>
            Archivo
            <input
              name="files"
              type="file"
              accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg"
              onChange={handleChange}
              multiple
              required
            />
            <span className="document-help">
              Formatos permitidos: PDF, PNG, JPG. Puedes seleccionar varios archivos.
            </span>
            {form.files.length > 0 && (
              <span className="document-help">{form.files.length} archivo(s) seleccionado(s)</span>
            )}
          </label>

          <div className="documents-actions">
            <button className="documents-primary" type="submit" disabled={submitting}>
              {submitting ? "Subiendo..." : "Subir documento"}
            </button>
            <Link className="documents-secondary" to="/documents">
              Cancelar
            </Link>
          </div>
        </form>
      </section>
    </main>
  );
};

export default DocumentFormPage;
