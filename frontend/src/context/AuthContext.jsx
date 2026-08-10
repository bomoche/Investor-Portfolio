import { createContext, useContext, useEffect, useMemo, useState } from "react";
import * as authApi from "../api/auth";
import { getStoredToken, clearToken } from "../api/client";

/**
 * Holds authentication state for the whole app.
 *
 * Context rather than prop drilling: the header, the route guard and the login
 * page all need this, and they sit at different depths in the tree.
 */
const AuthContext = createContext(null);

const INVESTOR_KEY = "enviro365.investor";

export function AuthProvider({ children }) {
  const [investor, setInvestor] = useState(null);
  const [initialising, setInitialising] = useState(true);

  /**
   * Restores the session on mount so a page refresh does not log the user out.
   * Both the token and the display details are needed — the token alone would
   * leave the header with no name to show until the next API call returns.
   */
  useEffect(() => {
    const token = getStoredToken();
    const stored = sessionStorage.getItem(INVESTOR_KEY);
    if (token && stored) {
      try {
        setInvestor(JSON.parse(stored));
      } catch {
        clearToken();
        sessionStorage.removeItem(INVESTOR_KEY);
      }
    }
    setInitialising(false);
  }, []);

  async function login(email, password) {
    const data = await authApi.login(email, password);
    const details = {
      investorId: data.investorId,
      fullName: data.fullName,
      email: data.email,
    };
    sessionStorage.setItem(INVESTOR_KEY, JSON.stringify(details));
    setInvestor(details);
    return details;
  }

  function logout() {
    authApi.logout();
    sessionStorage.removeItem(INVESTOR_KEY);
    setInvestor(null);
  }

  // Memoised so consumers do not re-render on every provider render.
  const value = useMemo(
    () => ({ investor, isAuthenticated: Boolean(investor), initialising, login, logout }),
    [investor, initialising]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside an AuthProvider");
  }
  return context;
}