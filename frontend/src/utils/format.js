/**
 * Formatting helpers shared across the app.
 *
 * Currency and dates are formatted in one place so every screen renders them
 * identically — the alternative is subtle drift between the dashboard, the
 * withdrawal form and the history table.
 */

/**
 * Formats a number as South African Rand.
 *
 * Uses Intl rather than string concatenation so thousands separators and the
 * decimal mark follow the en-ZA locale rather than being hard-coded.
 */
export function formatZAR(value) {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat("en-ZA", {
    style: "currency",
    currency: "ZAR",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/** Formats an ISO timestamp as a readable date, e.g. 10 Aug 2026. */
export function formatDate(isoString) {
  if (!isoString) return "—";
  return new Date(isoString).toLocaleDateString("en-ZA", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

/** Formats an ISO timestamp as date and time. */
export function formatDateTime(isoString) {
  if (!isoString) return "—";
  const date = new Date(isoString);
  return `${formatDate(isoString)}, ${date.toLocaleTimeString("en-ZA", {
    hour: "2-digit",
    minute: "2-digit",
  })}`;
}