import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useContext } from "react";
import { apiFetch } from "../api/api.js";
import { AuthContext } from "../context/AuthContext.jsx";
import theme from "../styles/theme";

function validateAuth(email, password, confirmPassword, mode, acceptedTerms, isOver18) {
  if (!email.trim()) return "Email is required";
  if (!email.includes("@") || !email.includes(".")) return "Invalid email format";
  if (!password) return "Password is required";

  if (mode === "register") {
    if (!confirmPassword) return "Please confirm your password";
    if (password.length < 8) return "Password must be at least 8 characters long";
    if (!/[A-Z]/.test(password)) return "Password must include at least one uppercase letter";
    if (!/[a-z]/.test(password)) return "Password must include at least one lowercase letter";
    if (!/[0-9]/.test(password)) return "Password must include at least one number";
    if (!/[!@#$%^&*()_+\-=\[\]{};':\",./<>?]/.test(password)) return "Password must include at least one special character";
    if (password !== confirmPassword) return "Passwords do not match";
    if (!acceptedTerms) return "You must accept the Terms and Conditions";
    if (!isOver18) return "You must confirm that you are over 18";
  }

  return null;
}

function Login({ initialMode = "login" }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [isOver18, setIsOver18] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [mode, setMode] = useState(initialMode === "register" ? "register" : "login");
  const { user, setUser } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const routeMode = location.pathname === "/register" ? "register" : "login";
    setMode(routeMode);
    setError("");
  }, [location.pathname]);

  useEffect(() => {
    if (user) {
      if (user.role === "ADMINISTRATOR") {
        navigate("/admin");
      } else {
        navigate("/dashboard");
      }
    }
  }, [user, navigate]);

  const isRegisterBlocked = mode === "register" && (!acceptedTerms || !isOver18 || !confirmPassword || password !== confirmPassword);

  async function handleAuth(e) {
    e.preventDefault();
    setError("");

    const validationError = validateAuth(email, password, confirmPassword, mode, acceptedTerms, isOver18);
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);

    try {
      const endpoint = mode === "register" ? "/api/auth/register" : "/api/auth/login";
      const data = await apiFetch(endpoint, {
        method: "POST",
        body: JSON.stringify({ email, password })
      });

      if (!data.accessToken) {
        setError(mode === "register" ? "Registration failed: No access token received" : "Login failed: No access token received");
        return;
      }

      localStorage.setItem("token", data.accessToken);

      const userData = await apiFetch("/api/users/me");
      setUser(userData);

      if(userData.role === "ADMINISTRATOR") {
        navigate("/admin");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      setError(err.message || (mode === "register" ? "Registration failed. Please try again." : "Login failed. Please check your credentials and try again."));
    } finally {
      setLoading(false);
    }
  }

  if (user !== null) {
    return <p>Welcome back, {user.email}! Redirecting...</p>;
  }

  return (
    <div style={{ backgroundColor: theme.backgroundWarm, minHeight: "100vh", padding: "30px 20px" }}>
      <div style={{ maxWidth: "480px", margin: "0 auto", backgroundColor: theme.backgroundWhite, border: `1px solid ${theme.border}`, borderRadius: "12px", boxShadow: "0 2px 8px rgba(0,0,0,0.06)", padding: "28px" }}>
        <h2 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 16px 0", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>
          {mode === "register" ? "Create Account" : "Login"}
        </h2>

        <div style={{ display: "flex", gap: "8px", marginBottom: "20px" }}>
          <button
            type="button"
            onClick={() => {
              setMode("login");
              navigate("/login");
              setError("");
            }}
            style={{ flex: 1, padding: "10px 12px", border: `1px solid ${theme.border}`, backgroundColor: mode === "login" ? theme.buttonPrimary : theme.backgroundWhite, color: mode === "login" ? theme.buttonPrimaryText : theme.textPrimary, borderRadius: "6px", cursor: "pointer", fontFamily: "'Arial', sans-serif", fontWeight: "500" }}
          >
            Login
          </button>
          <button
            type="button"
            onClick={() => {
              setMode("register");
              navigate("/register");
              setError("");
            }}
            style={{ flex: 1, padding: "10px 12px", border: `1px solid ${theme.border}`, backgroundColor: mode === "register" ? theme.buttonPrimary : theme.backgroundWhite, color: mode === "register" ? theme.buttonPrimaryText : theme.textPrimary, borderRadius: "6px", cursor: "pointer", fontFamily: "'Arial', sans-serif", fontWeight: "500" }}
          >
            Register
          </button>
        </div>

        {error && (
          <p style={{ color: theme.errorText, padding: "10px", backgroundColor: theme.errorBackground, borderRadius: "6px", marginBottom: "15px", fontFamily: "'Arial', sans-serif" }}>
            {error}
          </p>
        )}

        <form onSubmit={handleAuth}>
          <div style={{ marginBottom: "15px" }}>
            <label style={{ color: theme.textPrimary, fontWeight: "500", fontFamily: "'Arial', sans-serif" }}>Email:</label>
            <input
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (error) setError("");
              }}
              disabled={loading}
              style={{ width: "100%", padding: "10px", marginTop: "5px", border: `1px solid ${theme.border}`, borderRadius: "6px", boxSizing: "border-box", fontFamily: "'Arial', sans-serif" }}
            />
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label style={{ color: theme.textPrimary, fontWeight: "500", fontFamily: "'Arial', sans-serif" }}>Password:</label>
            <input
              type="password"
              placeholder={mode === "register" ? "Enter a strong password" : "Enter your password"}
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (error) setError("");
              }}
              disabled={loading}
              style={{ width: "100%", padding: "10px", marginTop: "5px", border: `1px solid ${theme.border}`, borderRadius: "6px", boxSizing: "border-box", fontFamily: "'Arial', sans-serif" }}
            />
          </div>

          {mode === "register" && (
            <div style={{ marginBottom: "15px" }}>
              <label style={{ color: theme.textPrimary, fontWeight: "500", fontFamily: "'Arial', sans-serif" }}>Confirm Password:</label>
              <input
                type="password"
                placeholder="Re-enter your password"
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value);
                  if (error) setError("");
                }}
                disabled={loading}
                style={{ width: "100%", padding: "10px", marginTop: "5px", border: `1px solid ${theme.border}`, borderRadius: "6px", boxSizing: "border-box", fontFamily: "'Arial', sans-serif" }}
              />
            </div>
          )}

          {mode === "register" && (
            <>
              <p style={{ color: theme.textMuted, margin: "0 0 15px 0", fontSize: "12px", fontFamily: "'Arial', sans-serif" }}>
                Password must be at least 8 characters and include an uppercase, lowercase, number, and special character.
              </p>

              <div style={{ marginBottom: "10px", display: "flex", alignItems: "center", gap: "8px" }}>
                <input
                  id="terms-checkbox"
                  type="checkbox"
                  checked={acceptedTerms}
                  onChange={(e) => {
                    setAcceptedTerms(e.target.checked);
                    if (error) setError("");
                  }}
                  disabled={loading}
                />
                <label htmlFor="terms-checkbox" style={{ color: theme.textPrimary, fontFamily: "'Arial', sans-serif", fontSize: "14px" }}>
                  I agree to the Terms and Conditions
                </label>
              </div>

              <div style={{ marginBottom: "15px", display: "flex", alignItems: "center", gap: "8px" }}>
                <input
                  id="age-checkbox"
                  type="checkbox"
                  checked={isOver18}
                  onChange={(e) => {
                    setIsOver18(e.target.checked);
                    if (error) setError("");
                  }}
                  disabled={loading}
                />
                <label htmlFor="age-checkbox" style={{ color: theme.textPrimary, fontFamily: "'Arial', sans-serif", fontSize: "14px" }}>
                  I confirm that I am over 18
                </label>
              </div>
            </>
          )}

          <button
            type="submit"
            disabled={loading || isRegisterBlocked}
            style={{
              width: "100%",
              padding: "10px",
              backgroundColor: loading || isRegisterBlocked ? theme.textMuted : theme.buttonPrimary,
              color: theme.buttonPrimaryText,
              border: "none",
              borderRadius: "6px",
              cursor: loading || isRegisterBlocked ? "not-allowed" : "pointer",
              opacity: loading || isRegisterBlocked ? 0.85 : 1,
              fontFamily: "'Arial', sans-serif",
              fontWeight: "500"
            }}
          >
            {loading ? (mode === "register" ? "Registering..." : "Logging in...") : (mode === "register" ? "Register" : "Login")}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;