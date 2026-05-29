import api from "./axios";

const buildFormData = ({ subject, content, files = [] }) => {
  const formData = new FormData();
  const data = new Blob([JSON.stringify({ subject, content })], {
    type: "application/json",
  });

  formData.append("data", data);
  files.forEach((file) => formData.append("files", file));

  return formData;
};

export const findMyMessages = ({ page = 0, size = 9 } = {}) =>
  api.get("/messages", { params: { page, size } });

export const findAllMessages = ({ page = 0, size = 9 } = {}) =>
  api.get("/messages/all", { params: { page, size } });

export const findMessageById = (messageId) => api.get(`/messages/${messageId}`);

export const createMessage = (message) => api.post("/messages", buildFormData(message));

export const respondMessage = (messageId, response) =>
  api.put(`/messages/${messageId}/response`, { response });

export const replyMessage = (messageId, response) =>
  api.post(`/messages/${messageId}/replies`, { response });

export const downloadMessageAttachment = (attachmentId) =>
  api.get(`/messages/attachments/${attachmentId}/download`, { responseType: "blob" });
