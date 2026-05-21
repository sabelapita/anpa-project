const fieldLabels = {
  userName: "usuario",
  password: "contraseña",
  firstName: "nombre",
  lastName: "apellidos",
  email: "correo electrónico",
};

export const getApiErrorMessage = (
  error,
  fallback = "No se pudo completar la operación. Inténtalo de nuevo."
) => {
  const data = error?.response?.data;

  if (!data) {
    return "No se pudo conectar con el servidor.";
  }

  if (data.globalError) {
    return data.globalError;
  }

  if (Array.isArray(data.fieldErrors) && data.fieldErrors.length > 0) {
    return data.fieldErrors
      .map(({ fieldName, message }) => {
        const label = fieldLabels[fieldName] || fieldName;
        return `${label}: ${message}`;
      })
      .join(" ");
  }

  return fallback;
};
