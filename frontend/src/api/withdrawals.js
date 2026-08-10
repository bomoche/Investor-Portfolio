import client from "./client";

export async function createWithdrawal(productId, amount) {
  const { data } = await client.post("/me/withdrawals", { productId, amount });
  return data;
}

export async function fetchWithdrawals() {
  const { data } = await client.get("/me/withdrawals");
  return data;
}

/**
 * Downloads the CSV statement.
 *
 * responseType 'blob' is required — the default would parse the response as
 * text and mangle the encoding. Undefined filters are stripped so the query
 * string carries only the filters actually in use.
 */
export async function downloadStatement(filters = {}) {
  const params = Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== "" && value != null)
  );

  const response = await client.get("/me/withdrawals/export", {
    params,
    responseType: "blob",
  });

  // The server supplies the filename; fall back only if the header is absent.
  const disposition = response.headers["content-disposition"] ?? "";
  const match = disposition.match(/filename="?([^"]+)"?/);
  const filename = match ? match[1] : "withdrawal-statement.csv";

  return { blob: response.data, filename };
}

/**
 * Triggers a browser download from an in-memory blob.
 *
 * The anchor is created, clicked and removed programmatically because there is
 * no server URL to link to — the file exists only as a blob in memory. The
 * object URL is revoked afterwards to release it.
 */
export function saveBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}