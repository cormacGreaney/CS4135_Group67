const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function messageFromApiError(errData) {
  if (errData?.fieldErrors && typeof errData.fieldErrors === "object") {
    const messages = Object.values(errData.fieldErrors).filter(Boolean);
    if (messages.length > 0) {
      return messages.join(" ");
    }
  }
  return errData?.message || null;
}

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

  // Auto-logout if unauthorized (expired/invalid session - not wrong password on change-password)
  if (res.status === 401) {
    const errData = await res.json().catch(() => ({}));
    const isPasswordChange = path === "/api/users/me/password";

    if (!isPasswordChange) {
      localStorage.removeItem("token");
      if (!isAuthEndpoint && !isAuthPage) {
        window.location.href = "/login";
      }
    }

    throw new Error(messageFromApiError(errData) || "Unauthorized");
  }

  if (!res.ok) {
    const errData = await res.json().catch(() => ({}));
    throw new Error(messageFromApiError(errData) || "API request failed");
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