export const isValidEmail = (email) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

export const hasMaxLength = (value, maxLength) => value.trim().length <= maxLength;
