import React, { useCallback, useContext, useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { findUsers, updateMembershipStatus, updateRole } from "../api/userApi";
import { AuthContext } from "../context/AuthContext";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./MessagesPage.css";

const roleLabels = {
  USER: "Usuario",
  ADMIN: "Administrador",
};

const membershipLabels = {
  NON_MEMBER: "No socio",
  MEMBER: "Socio",
};

const AdminUsersPage = () => {
  const { isAdmin, user: currentUser } = useContext(AuthContext);
  const [users, setUsers] = useState([]);
  const [existMoreItems, setExistMoreItems] = useState(false);
  const [page, setPage] = useState(0);
  const [keywords, setKeywords] = useState("");
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadUsers = useCallback(
    async (nextPage = 0, searchKeywords = keywords) => {
      setLoading(true);
      setError("");

      try {
        const response = await findUsers({
          page: nextPage,
          size: 10,
          keywords: searchKeywords,
        });

        setUsers(response.data.items || []);
        setExistMoreItems(Boolean(response.data.existMoreItems));
        setPage(nextPage);
      } catch (err) {
        setError(getApiErrorMessage(err, "No se pudieron cargar los usuarios."));
      } finally {
        setLoading(false);
      }
    },
    [keywords]
  );

  useEffect(() => {
    if (isAdmin) {
      loadUsers(0);
    }
  }, [isAdmin, loadUsers]);

  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  const replaceUser = (updatedUser) => {
    setUsers((current) =>
      current.map((user) => (user.id === updatedUser.id ? updatedUser : user))
    );
  };

  const handleMembershipChange = async (targetUser, value) => {
    setUpdatingId(targetUser.id);
    setMessage("");
    setError("");

    try {
      const response = await updateMembershipStatus(targetUser.id, value);
      replaceUser(response.data);
      setMessage("Condicion de socio actualizada.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo actualizar la condicion de socio."));
    } finally {
      setUpdatingId(null);
    }
  };

  const handleRoleChange = async (targetUser, value) => {
    const confirmed = window.confirm(
      value === "ADMIN"
        ? "El usuario pasara a tener permisos de administracion. Continuar?"
        : "El usuario dejara de tener permisos de administracion. Continuar?"
    );

    if (!confirmed) {
      return;
    }

    setUpdatingId(targetUser.id);
    setMessage("");
    setError("");

    try {
      const response = await updateRole(targetUser.id, value);
      replaceUser(response.data);
      setMessage("Rol actualizado correctamente.");
    } catch (err) {
      setError(getApiErrorMessage(err, "No se pudo actualizar el rol."));
    } finally {
      setUpdatingId(null);
    }
  };

  const handleSearch = (event) => {
    event.preventDefault();
    loadUsers(0);
  };

  return (
    <main className="messages-page">
      <header className="messages-header">
        <div>
          <p className="messages-kicker">Administracion</p>
          <h1>Usuarios y socios</h1>
          <p>
            Gestiona la condicion de socio y los permisos de administracion sin
            intervenir directamente en la base de datos.
          </p>
        </div>

        <div className="messages-actions">
          <Link className="messages-secondary" to="/dashboard">
            Mi panel
          </Link>
        </div>
      </header>

      {message && <div className="messages-alert success">{message}</div>}
      {error && <div className="messages-alert error">{error}</div>}

      <form className="messages-search" onSubmit={handleSearch}>
        <label htmlFor="user-search">Buscar por usuario</label>
        <div>
          <input
            id="user-search"
            type="search"
            value={keywords}
            onChange={(event) => setKeywords(event.target.value)}
            placeholder="Nombre de usuario"
            maxLength={60}
          />
          <button type="submit">Buscar</button>
          {keywords && (
            <button
              type="button"
              onClick={() => {
                setKeywords("");
                loadUsers(0, "");
              }}
            >
              Limpiar
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p className="messages-state">Cargando usuarios...</p>
      ) : users.length === 0 ? (
        <p className="messages-state">No hay usuarios que coincidan con la busqueda.</p>
      ) : (
        <section className="admin-users-panel">
          <div className="admin-users-table">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Nombre</th>
                  <th>Email</th>
                  <th>Socio</th>
                  <th>Rol</th>
                </tr>
              </thead>
              <tbody>
                {users.map((targetUser) => (
                  <tr key={targetUser.id}>
                    <td>{targetUser.userName}</td>
                    <td>{targetUser.firstName} {targetUser.lastName}</td>
                    <td>{targetUser.email}</td>
                    <td>
                      <select
                        value={targetUser.membershipStatus}
                        onChange={(event) =>
                          handleMembershipChange(targetUser, event.target.value)
                        }
                        disabled={updatingId === targetUser.id || targetUser.role === "ADMIN"}
                      >
                        <option value="NON_MEMBER">{membershipLabels.NON_MEMBER}</option>
                        <option value="MEMBER">{membershipLabels.MEMBER}</option>
                      </select>
                    </td>
                    <td>
                      <select
                        value={targetUser.role}
                        onChange={(event) => handleRoleChange(targetUser, event.target.value)}
                        disabled={updatingId === targetUser.id || targetUser.id === currentUser?.id}
                      >
                        <option value="USER">{roleLabels.USER}</option>
                        <option value="ADMIN">{roleLabels.ADMIN}</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      <div className="messages-pagination">
        <button type="button" onClick={() => loadUsers(page - 1)} disabled={page === 0}>
          Anterior
        </button>
        <span>Pagina {page + 1}</span>
        <button type="button" onClick={() => loadUsers(page + 1)} disabled={!existMoreItems}>
          Siguiente
        </button>
      </div>
    </main>
  );
};

export default AdminUsersPage;
