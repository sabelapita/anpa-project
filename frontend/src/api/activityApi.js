import api from "./axios";

export const findActivities = ({ page = 0, size = 10, keywords = "" } = {}) =>
  api.get("/activities", { params: { page, size, keywords: keywords || undefined } });

export const findActivityById = (activityId) => api.get(`/activities/${activityId}`);

export const createActivity = (activity) => api.post("/activities", activity);

export const updateActivity = (activityId, activity) =>
  api.put(`/activities/${activityId}`, activity);

export const deleteActivity = (activityId) => api.delete(`/activities/${activityId}`);
