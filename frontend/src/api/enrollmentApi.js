import api from "./axios";

export const enrollInActivity = (activityId) => api.post(`/enrollments/${activityId}`);

export const findMyEnrollments = () => api.get("/enrollments/my");

export const unsubscribeFromActivity = (activityId) =>
  api.delete(`/enrollments/${activityId}`);
