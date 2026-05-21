import { hasMaxLength, isValidEmail } from "./validators";

describe("validators", () => {
  test("validates email format", () => {
    expect(isValidEmail("familia@colegio.es")).toBe(true);
    expect(isValidEmail("familia@colegio")).toBe(false);
  });

  test("checks trimmed maximum length", () => {
    expect(hasMaxLength("  usuario  ", 7)).toBe(true);
    expect(hasMaxLength("usuario-largo", 7)).toBe(false);
  });
});
