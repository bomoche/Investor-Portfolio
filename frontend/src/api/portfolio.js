import client from "./client";

/**
 * Portfolio endpoints.
 *
 * No investor id in any path — the backend resolves the investor from the JWT,
 * so the client cannot request someone else's data by changing a URL.
 */
export async function fetchPortfolio() {
  const { data } = await client.get("/me/portfolio");
  return data;
}

export async function fetchEligibility(productId) {
  const { data } = await client.get(`/me/products/${productId}/eligibility`);
  return data;
}