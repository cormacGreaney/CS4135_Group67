import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/api.js";

function validateRegister(email, password) {
  if (!email.trim()) return "Email is required";
  if (!email.includes("@") || !email.includes(".")) return "Invalid email format";
  if (password.length < 8) return "Password must be at least 8 characters long";
  if (!/[A-Z]/.test(password)) return "Password must include at least one uppercase letter";
  if (!/[a-z]/.test(password)) return "Password must include at least one lowercase letter";
  if (!/[0-9]/.test(password)) return "Password must include at least one number";
  if (!/[!@#$%^&*()_+\-=\[\]{};':\",./<>?]/.test(password)) return "Password must include at least one special character";
  return null;
}

function Register() {
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
          if (userData.role === "ADMINISTRATOR") {
            navigate("/admin-dashboard");
          } else {
            navigate("/dashboard");
          }
        })
        .catch(() => {
          localStorage.removeItem("token");
        });
    }
  }, [navigate]);

  async function handleRegister(e) {
    e.preventDefault();
    setError("");

    const validationError = validateRegister(email, password);
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);

    try {
      const data = await apiFetch("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({ email, password })
      });

      if (!data.accessToken) {
        setError("Registration failed: No access token received");
        return;
      }

      localStorage.setItem("token", data.accessToken);
      const userData = await apiFetch("/api/users/me");
      setUser(userData);

      if (userData.role === "ADMINISTRATOR") {
        navigate("/admin-dashboard");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      setError(err.message || "Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  if (user !== null) {
    return <p>Welcome back, {user.email}! Redirecting...</p>;
  }

  return (
    <div style={{ padding: "20px" }}>
      <h2>Register</h2>

      {error && (
        <p style={{ color: "red", padding: "10px", backgroundColor: "#ffe0e0", borderRadius: "4px", marginBottom: "15px" }}>
          {error}
        </p>
      )}

      <form onSubmit={handleRegister}>
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
            placeholder="Enter a strong password"
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
            cursor: loading ? "not-allowed" : "pointer"
          }}
        >
          {loading ? "Registering..." : "Register"}
        </button>
      </form>
    </div>
  );
}

export default Register;