import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import TextField from "../components/common/TextField";
import Button from "../components/common/Button";
import Alert from "../components/common/Alert";
import DemoCredentials from "../components/DemoCredentials";

/**
 * Login screen, built to the Stitch layout.
 *
 * The design labelled the first field "Investor ID / Account Number", but the
 * backend authenticates on email, which is the unique login identifier on the
 * Investor entity. The field is relabelled accordingly rather than inventing a
 * second identifier the API cannot resolve.
 */
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({ email: "", password: "" });
  const [fieldErrors, setFieldErrors] = useState({});
  const [serverError, setServerError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
    // Clears the field's error as soon as the user starts correcting it.
    setFieldErrors((previous) => ({ ...previous, [name]: undefined }));
    setServerError(null);
  }

  /** Fills the form from the demo panel so a reviewer needn't retype. */
  function useAccount(email, password) {
    setForm({ email, password });
    setFieldErrors({});
    setServerError(null);
  }

  /**
   * Client-side validation mirrors the backend constraints so obvious mistakes
   * are caught without a round trip. The server remains the authority — this is
   * a convenience, not a substitute.
   */
  function validate() {
    const errors = {};
    if (!form.email.trim()) {
      errors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      errors.email = "Enter a valid email address";
    }
    if (!form.password) {
      errors.password = "Password is required";
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setServerError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      await login(form.email.trim(), form.password);
      // Returns the user to wherever the route guard intercepted them.
      const destination = location.state?.from?.pathname ?? "/dashboard";
      navigate(destination, { replace: true });
    } catch (error) {
      setServerError(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-full items-center justify-center bg-background p-4">
      <div className="w-full max-w-md py-8">
        <div className="rounded-xl border border-surface-variant bg-surface-container-lowest p-margin-lg shadow-[0_10px_15px_-3px_rgba(0,0,0,0.1)] md:p-pad-desktop">
          <div className="mb-8 flex flex-col items-center">
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary-container text-on-primary-container">
              <span className="material-symbols-outlined filled text-[28px]">eco</span>
            </div>
            <h1 className="mb-1 text-headline-lg text-primary">Enviro365</h1>
            <p className="text-body-md text-on-surface-variant">Investments</p>
          </div>

          {serverError && (
            <div className="mb-6">
              <Alert tone="error" title="Sign in failed" messages={[serverError]} />
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6" noValidate>
            <TextField
              id="email"
              name="email"
              type="email"
              label="Email Address"
              icon="person"
              placeholder="you@enviro365.co.za"
              autoComplete="username"
              value={form.email}
              onChange={handleChange}
              error={fieldErrors.email}
            />

            <TextField
              id="password"
              name="password"
              type="password"
              label="Password"
              icon="lock"
              placeholder="••••••••"
              autoComplete="current-password"
              value={form.password}
              onChange={handleChange}
              error={fieldErrors.password}
            />

            <Button type="submit" loading={submitting} className="w-full">
              {submitting ? "Signing in…" : "Access Portfolio"}
            </Button>
          </form>
        </div>

        <DemoCredentials onUseAccount={useAccount} />
      </div>
    </div>
  );
}