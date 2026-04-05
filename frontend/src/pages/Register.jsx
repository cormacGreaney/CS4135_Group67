import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "../api/api.js";

function validateEmail(email) {
  return /\S+@\S+\.\S+/.test(email);
}

function Register() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  async function handleRegister() {
    if (!validateEmail(email)) return alert("Invalid email format");
    if (password.length < 8) return alert("Password must be 8+ characters");

    try {
      const data = await apiFetch("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({ email, password })
      });
      localStorage.setItem("token", data.accessToken);
      navigate("/dashboard");
    } catch (err) {
      alert(err.message);
    }
  }

  return (
    <div style={{ padding: "20px" }}>
      <h2>Register</h2>
      <input placeholder="Email" onChange={e => setEmail(e.target.value)} />
      <input type="password" placeholder="Password" onChange={e => setPassword(e.target.value)} />
      <button onClick={handleRegister}>Register</button>
    </div>
  );
}

export default Register;