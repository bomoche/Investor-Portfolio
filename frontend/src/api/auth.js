import client, { storeToken, clearToken } from "./client";

export async function login(email, password) {
  const { data } = await client.post("/auth/login", { email, password });
  storeToken(data.token);
  return data;
}

export function logout() {
  clearToken();
}