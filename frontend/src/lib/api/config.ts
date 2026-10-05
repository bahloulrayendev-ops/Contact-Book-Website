/** Single source of truth for backend configuration. */
export const API_BASE_URL =
  (import.meta.env["VITE_API_BASE_URL"] as string | undefined) ?? "http://localhost:8080/api";

export const TOKEN_STORAGE_KEY = "dm.jwt";
