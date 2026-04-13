const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem("token");
  const isAuthEndpoint = path.startsWith("/api/auth/");
  const isAuthPage = window.location.pathname === "/login" || window.location.pathname === "/register";
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // Auto-logout if unauthorized
  if (res.status === 401) {
    localStorage.removeItem("token");
    const errData = await res.json().catch(() => ({}));

    if (!isAuthEndpoint && !isAuthPage) {
      window.location.href = "/login";
    }

    throw new Error(errData.message || "Unauthorized");
  }

  if (!res.ok) {
    const errData = await res.json().catch(() => ({}));
    throw new Error(errData.message || "API request failed");
  }

  // 204 No Content (e.g. DELETE) and empty bodies are valid - no JSON to parse
  if (res.status === 204 || res.status === 205) {
    return undefined;
  }

  const text = await res.text();
  if (!text.trim()) {
    return undefined;
  }
  return JSON.parse(text);
}

// Convenience logout function
export function logout() {
  localStorage.removeItem("token");
  window.location.href = "/login";
}