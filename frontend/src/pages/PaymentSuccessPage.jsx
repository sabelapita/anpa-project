import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { confirmCheckoutSession } from "../api/paymentApi";
import { getApiErrorMessage } from "../utils/apiErrors";
import "./PaymentResultPage.css";

const PaymentSuccessPage = () => {
  const [searchParams] = useSearchParams();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const sessionId = searchParams.get("session_id");

    if (!sessionId) {
      setError("No se recibio la referencia del pago.");
      setLoading(false);
      return;
    }

    confirmCheckoutSession(sessionId)
      .then((response) => setResult(response.data))
      .catch((err) =>
        setError(
          getApiErrorMessage(
            err,
            "No se pudo confirmar el pago. Revisa la inscripcion mas tarde."
          )
        )
      )
      .finally(() => setLoading(false));
  }, [searchParams]);

  return (
    <main className="payment-result-page">
      <section className="payment-result-panel">
        <p className="payment-result-kicker">Pago de actividad</p>
        <h1>{loading ? "Confirmando pago..." : "Pago realizado correctamente"}</h1>

        {loading && (
          <p>
            Estamos comprobando la operacion con Stripe para actualizar el estado
            de la inscripcion.
          </p>
        )}

        {!loading && result?.paid && (
          <p>
            La inscripcion ha quedado marcada como pagada. Ya puedes volver al
            detalle de la actividad o consultar el listado.
          </p>
        )}

        {!loading && result && !result.paid && (
          <p>
            Stripe recibio la vuelta del pago, pero la inscripcion todavia figura
            como pendiente. Puede actualizarse cuando llegue la confirmacion final.
          </p>
        )}

        {error && <div className="payment-result-alert">{error}</div>}

        <div className="payment-result-actions">
          {result?.activityId && (
            <Link to={`/activities/${result.activityId}`}>Ver actividad</Link>
          )}
          <Link to="/activities">Volver a actividades</Link>
        </div>
      </section>
    </main>
  );
};

export default PaymentSuccessPage;
