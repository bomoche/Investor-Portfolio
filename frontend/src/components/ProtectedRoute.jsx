import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Spinner from "./common/Spinner";

/**
 * Route guard.
 *
 * Waits for the session restore to finish before deciding — rendering the
 * redirect immediately would bounce an authenticated user to login on every
 * page refresh, before sessionStorage has been read.
 *
 * This is a convenience, not a security control. The API rejects unauthenticated
 * requests regardless of what the client renders.
 */
export default function ProtectedRoute({ children }) {
  const { isAuthenticated, initialising } = useAuth();
  const location = useLocation();

  if (initialising) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner size={32} className="text-primary" />
      </div>
    );
  }

  if (!isAuthenticated) {
    // Remembers where the user was headed so login can return them there.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}