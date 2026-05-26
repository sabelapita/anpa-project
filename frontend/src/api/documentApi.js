import api from "./axios";

export const findDocuments = ({ page = 0, size = 9, keywords = "" } = {}) =>
  api.get("/documents", { params: { page, size, keywords: keywords || undefined } });

export const uploadDocument = ({ title, description, files }) => {
  const formData = new FormData();
  const data = new Blob([JSON.stringify({ title, description })], {
    type: "application/json",
  });

  formData.append("data", data);
  files.forEach((file) => formData.append("files", file));

  return api.post("/documents", formData);
};

export const downloadDocument = (documentId) =>
  api.get(`/documents/${documentId}/download`, { responseType: "blob" });

export const deleteDocument = (documentId) => api.delete(`/documents/${documentId}`);
