import React, { useCallback, useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  deletePost,
  findAdminPosts,
  findPublishedPosts,
  setPostPublished,
} from "../api/blogApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./BlogPage.css";

const formatDate = (date) =>
  date
    ? new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }).format(new Date(date))
    : "";

const getShortContent = (content) => {
  if (!content || content.length <= 180) {
    return content;
  }

  return `${content.slice(0, 180).trim()}...`;
};

const BlogPage = () => {
  const { isAdmin } = useContext(AuthContext);
  const [posts, setPosts] = useState([]);
  const [existMoreItems, setExistMoreItems] = useState(false);
  const [page, setPage] = useState(0);
  const [keywords, setKeywords] = useState("");
  const [category, setCategory] = useState("");
  const [adminView, setAdminView] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const loadPosts = useCallback(
    async (nextPage = 0, nextKeywords = keywords, nextCategory = category, nextAdminView = adminView) => {
      setLoading(true);
      setError("");

      try {
        const response =
          isAdmin && nextAdminView
            ? await findAdminPosts({
                page: nextPage,
                size: 9,
                keywords: nextKeywords,
                category: nextCategory,
              })
            : await findPublishedPosts({
                page: nextPage,
                size: 9,
                keywords: nextKeywords,
                category: nextCategory,
              });

        setPosts(response.data.items || []);
        setExistMoreItems(Boolean(response.data.existMoreItems));
        setPage(nextPage);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudieron cargar las entradas del blog."));
      } finally {
        setLoading(false);
      }
    },
    [adminView, category, isAdmin, keywords]
  );

  useEffect(() => {
    loadPosts(0);
  }, [loadPosts]);

  const handleSearch = (event) => {
    event.preventDefault();
    loadPosts(0);
  };

  const handleAdminViewChange = (checked) => {
    setAdminView(checked);
    loadPosts(0, keywords, category, checked);
  };

  const handlePublishToggle = async (post) => {
    setBusyId(post.id);
    setMessage("");
    setError("");

    try {
      const response = await setPostPublished(post.id, !post.published);
      setPosts((current) =>
        current.map((item) => (item.id === post.id ? response.data : item))
      );
      setMessage(response.data.published ? "Entrada publicada correctamente." : "Entrada despublicada.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo cambiar el estado de publicacion."));
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async (post) => {
    const confirmed = window.confirm("Se eliminara la entrada del blog. Continuar?");

    if (!confirmed) {
      return;
    }

    setBusyId(post.id);
    setMessage("");
    setError("");

    try {
      await deletePost(post.id);
      setPosts((current) => current.filter((item) => item.id !== post.id));
      setMessage("Entrada eliminada correctamente.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo eliminar la entrada."));
    } finally {
      setBusyId(null);
    }
  };

  const clearFilters = () => {
    setKeywords("");
    setCategory("");
    loadPosts(0, "", "");
  };

  return (
    <main className="blog-page">
      <header className="blog-header">
        <div>
          <p className="blog-kicker">Blog del ANPA</p>
          <h1>Noticias y blogs tematicos</h1>
          <p>
            Consulta las entradas publicadas sobre excursiones, avisos del colegio
            y otros contenidos relevantes para las familias.
          </p>
        </div>

        <div className="blog-actions">
          <Link className="blog-secondary" to="/">
            Portada
          </Link>
          {isAdmin && (
            <Link className="blog-primary" to="/admin/blog/new">
              Nueva entrada
            </Link>
          )}
        </div>
      </header>

      {message && <div className="blog-alert success">{message}</div>}
      {error && <div className="blog-alert error">{error}</div>}

      <section className="blog-filter-panel">
        {isAdmin && (
          <label className="blog-admin-toggle">
            <input
              type="checkbox"
              checked={adminView}
              onChange={(event) => handleAdminViewChange(event.target.checked)}
            />
            Ver tambien entradas no publicadas
          </label>
        )}

        <form className="blog-search" onSubmit={handleSearch}>
          <label htmlFor="blog-search">Buscar por titulo</label>
          <div>
            <input
              id="blog-search"
              type="search"
              value={keywords}
              onChange={(event) => setKeywords(event.target.value)}
              placeholder="Titulo de la entrada"
              maxLength={100}
            />
            <input
              type="text"
              value={category}
              onChange={(event) => setCategory(event.target.value)}
              placeholder="Categoria"
              maxLength={50}
            />
            <button type="submit">Buscar</button>
            {(keywords || category) && (
              <button type="button" onClick={clearFilters}>
                Limpiar
              </button>
            )}
          </div>
        </form>
      </section>

      {loading ? (
        <p className="blog-state">Cargando entradas...</p>
      ) : posts.length === 0 ? (
        <p className="blog-state">No hay entradas que coincidan con la busqueda.</p>
      ) : (
        <section className="blog-grid" aria-label="Listado de entradas del blog">
          {posts.map((post) => (
            <article className="blog-card" key={post.id}>
              <div>
                <div className="blog-card-header">
                  <span>{post.category || "General"}</span>
                  {isAdmin && adminView && (
                    <span className={post.published ? "blog-status published" : "blog-status draft"}>
                      {post.published ? "Publicada" : "No publicada"}
                    </span>
                  )}
                </div>
                <h2>{post.title}</h2>
                <p>{getShortContent(post.content)}</p>
              </div>

              <dl>
                <div>
                  <dt>Fecha</dt>
                  <dd>{formatDate(post.createdAt)}</dd>
                </div>
                <div>
                  <dt>Adjuntos</dt>
                  <dd>{post.attachments?.length || 0}</dd>
                </div>
              </dl>

              <div className="blog-card-actions">
                <Link to={isAdmin && adminView ? `/admin/blog/${post.id}` : `/blog/${post.id}`}>
                  Ver detalle
                </Link>
                {isAdmin && (
                  <>
                    <Link to={`/admin/blog/${post.id}/edit`}>Editar</Link>
                    <button
                      type="button"
                      onClick={() => handlePublishToggle(post)}
                      disabled={busyId === post.id}
                    >
                      {post.published ? "Despublicar" : "Publicar"}
                    </button>
                    <button
                      type="button"
                      className="blog-danger"
                      onClick={() => handleDelete(post)}
                      disabled={busyId === post.id}
                    >
                      Eliminar
                    </button>
                  </>
                )}
              </div>
            </article>
          ))}
        </section>
      )}

      <div className="blog-pagination">
        <button type="button" onClick={() => loadPosts(page - 1)} disabled={page === 0}>
          Anterior
        </button>
        <span>Pagina {page + 1}</span>
        <button type="button" onClick={() => loadPosts(page + 1)} disabled={!existMoreItems}>
          Siguiente
        </button>
      </div>
    </main>
  );
};

export default BlogPage;
