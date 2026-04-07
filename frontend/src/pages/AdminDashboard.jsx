import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";
import { useNavigate } from "react-router-dom";
import theme from "../styles/theme";
import LogoutButton from "../components/LogoutButton.jsx";

function AdminDashboard() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);
  const [user, setUser] = useState(null);
  const [orderId, setOrderId] = useState("");
  const [orderStatus, setOrderStatus] = useState("PENDING");
  const [orderDetails, setOrderDetails] = useState(null);
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [activeTab, setActiveTab] = useState("products");
  const [productSort, setProductSort] = useState("name-asc");
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
    setMessage("");

    try {
      const data = await apiFetch("/api/products");
      setProducts(data.content || data || []);
    } catch (err) {
      setError(err.message || "Failed to load products");
    } finally {
      setLoading(false);
    }
  }

  async function loadOrderById(id) {
    if (!id.trim()) {
      setError("Order ID is required.");
      setOrders([]);
      setOrderDetails(null);
      setSelectedOrder(null);
      return;
    }

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const data = await apiFetch(`/api/order/${encodeURIComponent(id.trim())}`);
      setOrders([data]);
      setOrderDetails(data);
      selectOrder(data);
    } catch (err) {
      setError(err.message || "Failed to load order");
      setOrders([]);
      setOrderDetails(null);
      setSelectedOrder(null);
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
    setMessage("");
  }

  function handleInputChange(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (error) setError("");
    if (message) setMessage("");
  }

  function populateForm(product) {
    setEditingProduct(product);
    setForm({
      name: product.name || "",
      description: product.description || "",
      price: product.price?.toString() || "",
      stock: product.stockQuantity?.toString() || "",
      category: product.category || ""
    });
    setError("");
    setMessage("");
  }

  function selectOrder(order) {
    setSelectedOrder(order);
    setOrderDetails(order);
    setOrderId(order.id.toString());
    setOrderStatus(order.status);
  }

  function getSortedProducts() {
    return [...products].sort((a, b) => {
      switch (productSort) {
        case "name-asc":
          return a.name.localeCompare(b.name);
        case "name-desc":
          return b.name.localeCompare(a.name);
        case "category-asc":
          return a.category.localeCompare(b.category);
        case "category-desc":
          return b.category.localeCompare(a.category);
        case "price-asc":
          return (a.price || 0) - (b.price || 0);
        case "price-desc":
          return (b.price || 0) - (a.price || 0);
        case "stock-asc":
          return (a.stockQuantity || 0) - (b.stockQuantity || 0);
        case "stock-desc":
          return (b.stockQuantity || 0) - (a.stockQuantity || 0);
        default:
          return 0;
      }
    });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setMessage("");

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
      setMessage(editingProduct ? "Product updated successfully." : "Product created successfully.");
    } catch (err) {
      setError(err.message || "Unable to save product.");
    } finally {
      setSaving(false);
    }
  }

  async function updateOrderStatus(){
    if(!orderId.trim()) {
      setError("Order ID is required.");
      return;
    }
    setSaving(true);
    setError("");
    setMessage("");

    try {
      await apiFetch(`/api/order/${orderId}/status?status=${encodeURIComponent(orderStatus)}`, {
        method: "PUT"
      });
      await loadOrderById(orderId);
      setMessage("Order status updated successfully.");
    } catch (err) {
      setError(err.message || "Unable to update order status.");
    } finally {
      setSaving(false);
    }
  }

  async function cancelOrder() {
    if(!orderId.trim()) {
      setError("Order ID is required.");
      return;
    }
    setSaving(true);
    setError("");
    setMessage("");

    try {
      await apiFetch(`/api/order/${orderId}/cancel`, {
        method: "PUT"
      });
      await loadOrderById(orderId);
      setMessage("Order cancelled successfully.");
    } catch (err) {
      setError(err.message || "Unable to cancel order.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id) {
    const confirmed = window.confirm("Delete this product?");
    if (!confirmed) return;

    setSaving(true);
    setError("");
    setMessage("");

    try {
      await apiFetch(`/api/products/${id}`, {
        method: "DELETE"
      });
      await loadProducts();
      if (editingProduct?.id === id) resetForm();
      setMessage("Product deleted successfully.");
    } catch (err) {
      setError(err.message || "Unable to delete product.");
    } finally {
      setSaving(false);
    }
  }

  if (!user) return <p style={{ color: theme.textMuted, textAlign: "center", padding: "50px" }}>Loading admin dashboard...</p>;

  const totalProducts = products.length;
  const totalOrders = orders.length;
  const pendingOrders = orders.filter(o => o.status === "PENDING").length;
  const totalRevenue = orders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);

  return (
    <div style={{ backgroundColor: theme.backgroundWarm, minHeight: "100vh", padding: "20px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "6px" }}>
        <div style={{ flex: 1 }} />
        <h2 style={{ margin: 0 }}>Admin Dashboard</h2>
        <div style={{ flex: 1, display: "flex", justifyContent: "flex-end" }}>
          <LogoutButton />
        </div>
      </div>
      <p style={{ color: theme.textMuted, textAlign: "center", marginBottom: "30px" }}>Welcome, {user.email}</p>

      {error && (
        <p style={{ color: theme.errorText, backgroundColor: theme.errorBackground, padding: "15px", borderRadius: "8px", marginBottom: "20px", textAlign: "center" }}>
          {error}
        </p>
      )}

      {message && (
        <p style={{ color: theme.success, backgroundColor: theme.backgroundWhite, padding: "15px", borderRadius: "8px", marginBottom: "20px", textAlign: "center", border: `1px solid ${theme.border}` }}>
          {message}
        </p>
      )}

      {/* Stats Cards */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "20px", marginBottom: "30px" }}>
        <div style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}`, textAlign: "center" }}>
          <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 10px 0", fontSize: "18px" }}>Total Products</h3>
          <p style={{ color: theme.textAccent, fontSize: "24px", fontWeight: "bold", margin: 0 }}>{totalProducts}</p>
        </div>
        <div style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}`, textAlign: "center" }}>
          <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 10px 0", fontSize: "18px" }}>Total Orders</h3>
          <p style={{ color: theme.textAccent, fontSize: "24px", fontWeight: "bold", margin: 0 }}>{totalOrders}</p>
        </div>
        <div style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}`, textAlign: "center" }}>
          <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 10px 0", fontSize: "18px" }}>Pending Orders</h3>
          <p style={{ color: theme.textAccent, fontSize: "24px", fontWeight: "bold", margin: 0 }}>{pendingOrders}</p>
        </div>
        <div style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}`, textAlign: "center" }}>
          <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 10px 0", fontSize: "18px" }}>Total Revenue</h3>
          <p style={{ color: theme.success, fontSize: "24px", fontWeight: "bold", margin: 0 }}>${totalRevenue.toFixed(2)}</p>
        </div>
      </div>

      {/* Tab Navigation */}
      <div style={{ display: "flex", justifyContent: "center", marginBottom: "30px" }}>
        <button
          onClick={() => setActiveTab("products")}
          style={{
            padding: "12px 24px",
            backgroundColor: activeTab === "products" ? theme.buttonPrimary : theme.backgroundWhite,
            color: activeTab === "products" ? theme.buttonPrimaryText : theme.textPrimary,
            border: `1px solid ${theme.border}`,
            borderRadius: "8px 0 0 8px",
            cursor: "pointer",
            fontWeight: "500"
          }}
        >
          Products
        </button>
        <button
          onClick={() => setActiveTab("orders")}
          style={{
            padding: "12px 24px",
            backgroundColor: activeTab === "orders" ? theme.buttonPrimary : theme.backgroundWhite,
            color: activeTab === "orders" ? theme.buttonPrimaryText : theme.textPrimary,
            border: `1px solid ${theme.border}`,
            borderLeft: "none",
            borderRadius: "0 8px 8px 0",
            cursor: "pointer",
            fontWeight: "500"
          }}
        >
          Orders
        </button>
      </div>

      {}
      {activeTab === "products" && (
        <>
          <section style={{ marginBottom: "20px", backgroundColor: theme.backgroundWhite, padding: "30px", borderRadius: "12px", border: `1px solid ${theme.border}`, boxShadow: "0 2px 8px rgba(0,0,0,0.1)", maxWidth: "500px", marginLeft: "auto", marginRight: "auto" }}>
            <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, marginBottom: "25px", fontSize: "22px", textAlign: "center", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>
              {editingProduct ? "Edit Product" : "Add New Product"}
            </h3>
            <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
              <div>
                <label style={{ fontFamily: "'Georgia', serif",  color: theme.textPrimary, fontWeight: "600", display: "block", marginBottom: "8px", fontSize: "14px" }}>Product Name</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => handleInputChange("name", e.target.value)}
                  disabled={saving}
                  placeholder="Enter product name"
                  style={{ fontFamily: "'Arial', sans-serif", width: "100%", padding: "12px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "16px", transition: "border-color 0.2s" }}
                />
              </div>

              <div>
                <label style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, fontWeight: "600", display: "block", marginBottom: "8px", fontSize: "14px" }}>Description</label>
                <textarea
                  value={form.description}
                  onChange={(e) => handleInputChange("description", e.target.value)}
                  disabled={saving}
                  placeholder="Describe the product"
                  style={{fontFamily: "'Arial', sans-serif", width: "100%", padding: "12px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, minHeight: "100px", fontSize: "16px", resize: "vertical", transition: "border-color 0.2s" }}
                />
              </div>

              <div style={{ display: "flex", gap: "15px" }}>
                <div style={{ flex: 1 }}>
                  <label style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, fontWeight: "600", display: "block", marginBottom: "8px", fontSize: "14px" }}>Category</label>
                  <input
                    type="text"
                    value={form.category}
                    onChange={e => handleInputChange("category", e.target.value)}
                    disabled={saving}
                    placeholder="e.g., Alcohol"
                    style={{ fontFamily: "'Arial', sans-serif", width: "100%", padding: "12px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "16px", transition: "border-color 0.2s" }}
                  />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, fontWeight: "600", display: "block", marginBottom: "8px", fontSize: "14px" }}>Price (€)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={form.price}
                    onChange={e => handleInputChange("price", e.target.value)}
                    disabled={saving}
                    placeholder="0.00"
                    style={{fontFamily: "'Arial', sans-serif", width: "100%", padding: "12px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "16px", transition: "border-color 0.2s" }}
                  />
                </div>
              </div>

              <div>
                <label style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, fontWeight: "600", display: "block", marginBottom: "8px", fontSize: "14px" }}>Stock Quantity</label>
                <input
                  type="number"
                  value={form.stock}
                  onChange={e => handleInputChange("stock", e.target.value)}
                  disabled={saving}
                  placeholder="0"
                  style={{ fontFamily: "'Arial', sans-serif", width: "100%", padding: "12px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "16px", transition: "border-color 0.2s" }}
                />
              </div>

              <div style={{ display: "flex", gap: "10px", justifyContent: "flex-end", marginTop: "10px" }}>
                <button type="submit" disabled={saving} style={{ fontFamily: "'Arial', sans-serif", padding: "12px 24px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "6px", cursor: saving ? "not-allowed" : "pointer", fontWeight: "500", fontSize: "16px", transition: "background-color 0.2s" }}>
                  {saving ? "Saving..." : editingProduct ? "Update Product" : "Add Product"}
                </button>

                {editingProduct && (
                  <button type="button" 
                    onClick={resetForm} 
                    disabled={saving} 
                    style={{ padding: "12px 24px", backgroundColor: theme.textMuted, color: theme.buttonPrimaryText, border: "none", borderRadius: "6px", cursor: saving ? "not-allowed" : "pointer", fontWeight: "600", fontSize: "16px", transition: "background-color 0.2s" }}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </section>

          <section style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
            <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 18px 0", fontSize: "20px", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>Product List</h3>
            <div style={{ marginBottom: "20px" }}>
              <select
                value={productSort}
                onChange={e => setProductSort(e.target.value)}
                style={{ padding: "8px", marginRight: "10px", border: `1px solid ${theme.border}`, borderRadius: "4px", backgroundColor: theme.backgroundWhite }}
              >
                <option value="name-asc">Name A-Z</option>
                <option value="name-desc">Name Z-A</option>
                <option value="category-asc">Category A-Z</option>
                <option value="category-desc">Category Z-A</option>
                <option value="price-asc">Price Low-High</option>
                <option value="price-desc">Price High-Low</option>
                <option value="stock-asc">Stock Low-High</option>
                <option value="stock-desc">Stock High-Low</option>
              </select>
            </div>
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Name</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Category</th>
                    <th style={{ textAlign: "right", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Price</th>
                    <th style={{ textAlign: "right", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Stock</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {getSortedProducts().map(product => (
                    <tr key={product.id} style={{ ":hover": { backgroundColor: theme.backgroundWarm } }}>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{product.name}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{product.category}</td>
                      <td style={{ padding: "10px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{product.price?.toFixed(2)}</td>
                      <td style={{ padding: "10px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{product.stockQuantity}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}` }}>
                        <button
                          onClick={() => populateForm(product)}
                          disabled={saving}
                          style={{ marginRight: "8px", padding: "6px 10px", borderRadius: "4px", border: `1px solid ${theme.buttonPrimary}`, backgroundColor: theme.backgroundWhite, color: theme.buttonPrimary, cursor: "pointer" }}
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDelete(product.id)}
                          disabled={saving}
                          style={{ padding: "6px 10px", borderRadius: "4px", border: `1px solid ${theme.errorText}`, backgroundColor: theme.errorBackground, color: theme.errorText, cursor: "pointer" }}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}

      {activeTab === "orders" && (
        <>
          <section style={{ marginBottom: "20px", backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
            <h3 style={{ color: theme.textPrimary, marginBottom: "15px" }}>Order Lookup and Management</h3>
            <div style={{ maxWidth: "600px", boxSizing: "border-box" }}>
              <label style={{ color: theme.textPrimary, fontWeight: "500" }}>Order ID:</label>
              <input
                type="text"
                value={orderId}
                onChange={e => {
                  setOrderId(e.target.value);
                  if (error) setError("");
                  if (message) setMessage("");
                }}
                disabled={saving}
                style={{ width: "100%", padding: "12px", marginTop: "5px", border: `1px solid ${theme.border}`, borderRadius: "4px", backgroundColor: theme.backgroundWhite, boxSizing: "border-box" }}
              />
              <button
                onClick={() => loadOrderById(orderId)}
                disabled={saving || loading}
                style={{ padding: "10px 16px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: saving || loading ? "not-allowed" : "pointer", marginTop: "10px" }}
              >
                {loading ? "Loading..." : "Find Order"}
              </button>
              <label style={{ color: theme.textPrimary, fontWeight: "500", marginTop: "10px", display: "block" }}>Status:</label>
              <select
                value={orderStatus}
                onChange={e => setOrderStatus(e.target.value)}
                disabled={saving}
                style={{ width: "100%", padding: "12px", marginTop: "5px", border: `1px solid ${theme.border}`, borderRadius: "4px", backgroundColor: theme.backgroundWhite, boxSizing: "border-box"  }}
              >
                <option value="PENDING">PENDING</option>
                <option value="SHIPPED">SHIPPED</option>
                <option value="DELIVERED">DELIVERED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
              <button
                onClick={updateOrderStatus}
                disabled={saving}
                style={{ padding: "10px 16px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer", marginTop: "10px" }}
              >
                Update Status
              </button>
              <button
                onClick={cancelOrder}
                disabled={saving}
                style={{ padding: "10px 16px", backgroundColor: theme.errorText, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer", marginTop: "10px", marginLeft: "10px" }}
              >
                Cancel Order
              </button>
            </div>
          </section>

          <section style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
            <h3 style={{ color: theme.textPrimary, marginBottom: "15px" }}>Loaded Order</h3>
            {!orders.length && (
              <p style={{ color: theme.textMuted, marginTop: 0 }}>Search by order ID to view and manage an order.</p>
            )}
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Order ID</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>User ID</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Status</th>
                    <th style={{ textAlign: "right", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Total Price</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Ordered Date</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map(order => (
                    <tr key={order.id} style={{ cursor: "pointer" }} onClick={() => selectOrder(order)}>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{order.id}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{order.userId}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{order.status}</td>
                      <td style={{ padding: "10px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>€{order.totalPrice?.toFixed(2)}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{new Date(order.orderedDate).toLocaleDateString()}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}` }}>
                        <button
                          onClick={(e) => { e.stopPropagation(); selectOrder(order); }}
                          style={{ padding: "6px 10px", borderRadius: "4px", border: `1px solid ${theme.buttonPrimary}`, backgroundColor: theme.backgroundWhite, color: theme.buttonPrimary, cursor: "pointer" }}
                        >
                          Edit
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {orderDetails && (
              <div style={{ marginTop: "20px", paddingTop: "20px", borderTop: `1px solid ${theme.border}` }}>
                <h4 style={{ color: theme.textPrimary, marginTop: 0 }}>Order Details</h4>
                <p style={{ color: theme.textPrimary, margin: "0 0 8px 0" }}><strong>Order Number:</strong> {orderDetails.orderNumber}</p>
                <p style={{ color: theme.textPrimary, margin: "0 0 8px 0" }}><strong>Status:</strong> {orderDetails.status}</p>
                <p style={{ color: theme.textPrimary, margin: 0 }}><strong>Total Price:</strong> €{orderDetails.totalPrice?.toFixed(2)}</p>
              </div>
            )}
          </section>
        </>
      )}

    </div>
  );
}

export default AdminDashboard;