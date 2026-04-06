import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/api.js";

function validateLogin(email, password) {
  if (!email.trim()) return "Email is required";
  if (!email.includes("@") || !email.includes(".")) return "Invalid email format";
  if (!password) return "Password is required";
  return null;
}

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      apiFetch("/api/users/me")
        .then((userData) => {
          setUser(userData);
          if (userData.role === "ADMINISTRATOR") navigate("/admin");
          else navigate("/dashboard");
        })
        .catch(() => {
          localStorage.removeItem("token");
        });
    }
  }, [navigate]);

  async function handleLogin(e) {
    e.preventDefault();
    setError("");

    const validationError = validateLogin(email, password);
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      const data = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });

      if (!data.accessToken) throw new Error("No access token received");

      localStorage.setItem("token", data.accessToken);

      const userData = await apiFetch("/api/users/me");
      setUser(userData);

      if (userData.role === "ADMINISTRATOR") navigate("/admin");
      else navigate("/dashboard");
    } catch (err) {
      setError(err.message || "Login failed. Check your credentials.");
    } finally {
      setLoading(false);
    }
  }

  if (user !== null) {
    return <p>Welcome back, {user.email}! Redirecting...</p>;
  }

  return (
    <div style={{ padding: "20px", maxWidth: "400px", margin: "0 auto" }}>
      <h2>Login</h2>

      {error && (
        <p style={{ color: "red", padding: "10px", backgroundColor: "#ffe0e0", borderRadius: "4px", marginBottom: "15px" }}>
          {error}
        </p>
      )}

      <form onSubmit={handleLogin}>
        <div style={{ marginBottom: "15px" }}>
          <label>Email:</label>
          <input
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (error) setError("");
            }}
            disabled={loading}
            style={{ width: "100%", padding: "8px", marginTop: "5px" }}
          />
        </div>

        <div style={{ marginBottom: "15px" }}>
          <label>Password:</label>
          <input
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (error) setError("");
            }}
            disabled={loading}
            style={{ width: "100%", padding: "8px", marginTop: "5px" }}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          style={{
            width: "100%",
            padding: "10px",
            backgroundColor: loading ? "#ccc" : "#007bff",
            color: "#fff",
            border: "none",
            borderRadius: "4px",
            cursor: loading ? "not-allowed" : "pointer",
          }}
        >
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
    </div>
  );
}

export default Login;