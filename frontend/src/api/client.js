import axios from "axios";

/**
 * Shared axios instance for every API call.
 *
 * The base URL is the relative /api path, which the Vite dev proxy forwards to
 * Spring Boot. Using a relative path means the browser sees same-origin
 * requests in development and the build needs no environment-specific rebuild.
 */
const client = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

const TOKEN_KEY = "enviro365.token";

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function storeToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  sessionStorage.removeItem(TOKEN_KEY);
}

/**
 * Request interceptor: attaches the bearer token to every outgoing call.
 *
 * Centralised here so no individual API module has to remember the header —
 * forgetting it in one place would produce a confusing 401 on a single screen.
 *
 * sessionStorage rather than localStorage: the token is cleared when the tab
 * closes, which narrows the window in which a token left on a shared machine
 * is usable. Neither is immune to XSS; an httpOnly cookie would be the
 * production answer, at the cost of reintroducing CSRF concerns.
 */
client.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Response interceptor: normalises errors into a predictable shape.
 *
 * The backend returns one ErrorResponse contract for every failure, so this
 * unwraps it once and hands components a consistent object rather than making
 * each screen dig through error.response.data itself.
 *
 * A 401 means the token is missing or expired, so the session is cleared and
 * the app redirected to login. A 403 is deliberately left alone — the user is
 * authenticated, just not permitted, and logging them out would be wrong.
 */
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const data = error.response?.data;

    if (status === 401 && !error.config?.url?.includes("/auth/login")) {
      clearToken();
      window.location.href = "/login";
    }

    return Promise.reject({
      status,
      errorCode: data?.errorCode ?? "NETWORK_ERROR",
      message:
        data?.message ??
        "Unable to reach the server. Please check your connection and try again.",
      fieldErrors: data?.fieldErrors ?? [],
    });
  }
);

export default client;