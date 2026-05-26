// src/App.js
import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import PublicHomePage from "./pages/PublicHomePage";
import ActivitiesPage from "./pages/ActivitiesPage";
import ActivityDetailPage from "./pages/ActivityDetailPage";
import ActivityFormPage from "./pages/ActivityFormPage";
import DocumentsPage from "./pages/DocumentsPage";
import DocumentFormPage from "./pages/DocumentFormPage";
import PaymentSuccessPage from "./pages/PaymentSuccessPage";
import PaymentCancelPage from "./pages/PaymentCancelPage";
import ProtectedRoute from "./components/ProtectedRoute";
import "bootstrap/dist/css/bootstrap.min.css";
import "./index.css";
import SignupPage from "./pages/SignupPage";

const App = () => {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<PublicHomePage />} />
          <Route path="/activities" element={<ActivitiesPage />} />
          <Route path="/activities/:id" element={<ActivityDetailPage />} />
          <Route
            path="/payment/success"
            element={
              <ProtectedRoute>
                <PaymentSuccessPage />
              </ProtectedRoute>
            }
          />
          <Route path="/payment/cancel" element={<PaymentCancelPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <HomePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/documents"
            element={
              <ProtectedRoute>
                <DocumentsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/documents/new"
            element={
              <ProtectedRoute>
                <DocumentFormPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/activities/new"
            element={
              <ProtectedRoute>
                <ActivityFormPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/activities/:id/edit"
            element={
              <ProtectedRoute>
                <ActivityFormPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </Router>
  );
};

export default App;
