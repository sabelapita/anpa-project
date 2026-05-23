import React from "react";
import { Link, useSearchParams } from "react-router-dom";
import "./PaymentResultPage.css";

const PaymentCancelPage = () => {
  const [searchParams] = useSearchParams();
  const enrollmentId = searchParams.get("enrollmentId");

  return (
    <main className="payment-result-page">
      <section className="payment-result-panel">
        <p className="payment-result-kicker">Pago de actividad</p>
        <h1>Pago cancelado</h1>
        <p>
          No se completo el pago en Stripe. La inscripcion queda pendiente y
          puedes volver al listado para intentarlo de nuevo.
        </p>

        {enrollmentId && (
          <p className="payment-result-note">
            Referencia de inscripcion: <strong>{enrollmentId}</strong>
          </p>
        )}

        <div className="payment-result-actions">
          <Link to="/activities">Volver a actividades</Link>
        </div>
      </section>
    </main>
  );
};

export default PaymentCancelPage;
