import api from "./axios";

export const loginUser = ({ userName, password }) =>
  api.post("/users/login", { userName, password });

export const signUpUser = ({ userName, password, firstName, lastName, email }) =>
  api.post("/users/signUp", {
    userName,
    password,
    firstName,
    lastName,
    email,
  });

export const loginFromServiceToken = () => api.post("/users/loginFromServiceToken");

export const requestPasswordReset = ({ email }) =>
  api.post("/users/forgotPassword", { email });

export const resetPassword = ({ token, newPassword }) =>
  api.post("/users/resetPassword", { token, newPassword });
