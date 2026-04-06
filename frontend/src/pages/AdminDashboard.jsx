import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";
import { useNavigate } from "react-router-dom";

function AdminDashboard() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    apiFetch("/api/users/me")
      .then(u => {
        if (u.role !== "ADMINISTRATOR") { 
          navigate("/");
        } else {
          setUser(u);
          loadProducts();
        }
      })
      .catch(() => {
        navigate("/login");
      });
  }, [navigate]);

async function loadProducts() {
  setLoading(true);
  setError("");

  try {
    const data = await apiFetch("/api/products");
    setProducts(data.content || data || []);
  } catch (err) {
    setError(err.message || "Failed to load products");
  } finally {
    setLoading(false);
  }
}

  if (!user) return <p>Loading...</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Admin Dashboard</h2>
      <p>Welcome, {user.email}</p>
      <p>You can manage products, orders, etc. here.</p>
    </div>
  );
}

export default AdminDashboard;