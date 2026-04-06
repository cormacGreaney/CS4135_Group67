import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";
import { useNavigate } from "react-router-dom";

function AdminDashboard() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
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

  const [form, setForm] = useState({
    name: "",
    description: "",
    price: "",
    stock: "",
    category: ""
  });
  const [editingProduct, setEditingProduct] = useState(null);

  function resetForm() {
    setForm({ name: "", description: "", price: "", stock: "", category: "" });
    setEditingProduct(null);
    setError("");
  }

  function handleInputChange(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (error) setError("");
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!form.name.trim()) {
      setError("Product name is required.");
      return;
    }
    if (!form.category.trim()) {
      setError("Product category is required.");
      return;
    }
    if (!form.price || isNaN(Number(form.price))) {
      setError("Valid product price is required.");
      return;
    }
    if (!form.stock || isNaN(Number(form.stock))) {
      setError("Valid stock quantity is required.");
      return;
    }

    setSaving(true);

    const payload = {
      name: form.name,
      description: form.description,
      price: Number(form.price),
      stockQuantity: Number(form.stock),
      category: form.category
    };

    try {
      if (editingProduct) {
        await apiFetch(`/api/products/${editingProduct.id}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await apiFetch("/api/products", {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }

      await loadProducts();
      resetForm();
    } catch (err) {
      setError(err.message || "Unable to save product.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id) {
    const confirmed = window.confirm("Delete this product?");
    if (!confirmed) return;

    setSaving(true);
    setError("");

    try {
      await apiFetch(`/api/products/${id}`, {
        method: "DELETE"
      });
      await loadProducts();
      if (editingProduct?.id === id) resetForm();
    } catch (err) {
      setError(err.message || "Unable to delete product.");
    } finally {
      setSaving(false);
    }
  }

  if (!user) return <p>Loading admin dashboard...</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Admin Dashboard</h2>
      <p>Welcome, {user.email}</p>

      {error && (
        <p style={{ color: "red", backgroundColor: "#ffe0e0", padding: "10px", borderRadius: "4px" }}>
          {error}
        </p>
      )}

      <section style ={{ marginBottom: "20px" }}>
        <h3>{editingProduct ? "Edit Product" : "Add Product"}</h3>
        <form onSubmit={handleSubmit} style={{ maxWidth: "600px" }}>
          <div style={{ marginBottom: "12px" }}>
            <label>Name</label>
            <input
              type="text"
              value={form.name}
              onChange={(e) => handleInputChange("name", e.target.value)}
              disabled={saving}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ marginBottom: "12px" }}>
            <label>Description</label>
            <textarea
              value={form.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              disabled={saving}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px", marginBottom: "12px" }}>
            <div>
              <label>Category</label>
              <input
                type="text"
                value={form.category}
                onChange={e => handleInputChange("category", e.target.value)}
                disabled={saving}
                style={{ width: "100%", padding: "8px", marginTop: "5px" }}
              />
            </div>
            <div>
              <label>Price</label>
              <input
                type="number"
                step="0.01"
                value={form.price}
                onChange={e => handleInputChange("price", e.target.value)}
                disabled={saving}
                style={{ width: "100%", padding: "8px", marginTop: "5px" }}
              />
            </div>
            <div>
              <label>Stock</label>
              <input
                type="number"
                value={form.stock}
                onChange={e => handleInputChange("stock", e.target.value)}
                disabled={saving}
                style={{ width: "100%", padding: "8px", marginTop: "5px" }}
              />
            </div>
          </div>

          <button type="submit" disabled={saving} style={{ padding: "10px 16px", backgroundColor: "#007bff", color: "#fff", border: "none", borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer" }}>
            {saving ? "Saving..." : editingProduct ? "Save changes" : "Create product"}
          </button>

          {editingProduct && (
            <button type="button" 
              onClick={resetForm} 
              disabled={saving} 
              style={{ padding: "10px 16px", backgroundColor: "#6c757d", color: "#fff", border: "none", borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer", marginLeft: "10px" }}
            >
              Cancel
            </button>
          )}
        </form>
      </section>
    </div>
  );
}

export default AdminDashboard;