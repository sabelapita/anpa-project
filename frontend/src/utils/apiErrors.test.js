import { getApiErrorMessage } from "./apiErrors";

describe("getApiErrorMessage", () => {
  test("returns global backend errors", () => {
    const error = {
      response: {
        data: {
          globalError: "Usuario o contraseña erróneos.",
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe("Usuario o contraseña erróneos.");
  });

  test("formats backend field errors", () => {
    const error = {
      response: {
        data: {
          fieldErrors: [{ fieldName: "email", message: "must be a well-formed email address" }],
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe(
      "correo electrónico: must be a well-formed email address"
    );
  });

  test("returns network error message when response is missing", () => {
    expect(getApiErrorMessage({})).toBe("No se pudo conectar con el servidor.");
  });
});
