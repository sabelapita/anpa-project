import api from "./axios";

export const findUsers = ({ page = 0, size = 10, keywords = "" } = {}) =>
  api.get("/users", { params: { page, size, keywords: keywords || undefined } });

export const updateMembershipStatus = (userId, value) =>
  api.patch(`/users/${userId}/membership`, null, { params: { value } });

export const updateRole = (userId, value) =>
  api.patch(`/users/${userId}/role`, null, { params: { value } });
