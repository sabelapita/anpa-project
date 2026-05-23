import api from "./axios";

export const createCheckoutSession = (enrollmentId) =>
  api.post(`/payments/checkout/enrollments/${enrollmentId}`);

export const confirmCheckoutSession = (sessionId) =>
  api.post(`/payments/checkout/sessions/${sessionId}/confirm`);
