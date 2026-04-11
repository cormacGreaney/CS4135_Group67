import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";
import { useNavigate } from "react-router-dom";
import theme from "../styles/theme";
import LogoutButton from "../components/LogoutButton.jsx";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function formatCurrency(value) {
  return `€${Number(value ?? 0).toFixed(2)}`;
}

function formatDate(value) {
  if (!value) {
    return "N/A";
  }

  return new Date(value).toLocaleDateString();
}

function statusBadge(status) {
  const colours = {
    PENDING: { bg: "#fff8e1", text: "#b8860b" },
    PAID: { bg: "#e8f0fe", text: "#1a56db" },
    SHIPPED: { bg: "#e8f0fe", text: "#1a56db" },
    DELIVERED: { bg: "#e6f4ea", text: "#2e7d32" },
    CANCELLED: { bg: theme.errorBackground, text: theme.errorText },
  };
  const colour = colours[status] || { bg: theme.backgroundWarm, text: theme.textMuted };

  return (
    <span style={{ display: "inline-block", padding: "3px 10px", borderRadius: "12px", fontSize: "12px", fontWeight: "600", letterSpacing: "0.04em", backgroundColor: colour.bg, color: colour.text }}>
      {status}
    </span>
  );
}

function AdminDashboard() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);
  const [user, setUser] = useState(null);
  const [orderLookupNumber, setOrderLookupNumber] = useState("");
  const [orderStatus, setOrderStatus] = useState("PENDING");
  const [orderDetails, setOrderDetails] = useState(null);
  const [orders, setOrders] = useState([]);
  const [allOrders, setAllOrders] = useState([]);
  const [pendingOrdersCount, setPendingOrdersCount] = useState(0);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [activeTab, setActiveTab] = useState("products");
  const [productSort, setProductSort] = useState("name-asc");
  const [allOrdersFilter, setAllOrdersFilter] = useState("ALL");
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [changingPassword, setChangingPassword] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    apiFetch("/api/users/me")
      .then(u => {
        if (u.role !== "ADMINISTRATOR") { 
          navigate("/");
        } else {
          setUser(u);
          loadProducts();
          loadAllOrders();
          loadPendingOrdersCount();
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
      setError("Order number is required.");
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

  async function loadOrderByNumber(orderNumber) {
    const lookupValue = String(orderNumber ?? "").trim();

    if (!lookupValue) {
      setError("Order number is required.");
      setOrders([]);
      setOrderDetails(null);
      setSelectedOrder(null);
      return;
    }

    const normalizedLookup = lookupValue.replace(/^#/, "");
    const parsedOrderNumber = Number.parseInt(normalizedLookup, 10);

    if (!Number.isInteger(parsedOrderNumber) || parsedOrderNumber < 1) {
      setError("Order number must be a positive number.");
      setOrders([]);
      setOrderDetails(null);
      setSelectedOrder(null);
      return;
    }

    const matchedOrder = allOrders[parsedOrderNumber - 1];

    if (!matchedOrder?.id) {
      setError("Order number not found.");
      setOrders([]);
      setOrderDetails(null);
      setSelectedOrder(null);
      return;
    }

    await loadOrderById(String(matchedOrder.id));
  }

  async function loadAllOrders() {
    setError("");

    try {
      const data = await apiFetch("/api/order/allOrders");
      const orderList = Array.isArray(data) ? data : [];
      setAllOrders(orderList);
      setPendingOrdersCount(orderList.filter(o => String(o.status || "").toUpperCase() === "PENDING").length);
    } catch (err) {
      setError(err.message || "Failed to load all orders");
      setAllOrders([]);
    }
  }

  async function loadPendingOrdersCount() {
    try {
      const data = await apiFetch("/api/order/orderBy/PENDING");
      const pendingList = Array.isArray(data) ? data : [];
      setPendingOrdersCount(pendingList.length);
    } catch {
        setPendingOrdersCount(0);
    }
  }

  function handlePasswordInputChange(field, value) {
    setPasswordForm((current) => ({ ...current, [field]: value }));
    if (passwordError) setPasswordError("");
    if (passwordMessage) setPasswordMessage("");
  }

  async function handleChangePassword(e) {
    e.preventDefault();
    setPasswordError("");
    setPasswordMessage("");

    const currentPassword = passwordForm.currentPassword.trim();
    const newPassword = passwordForm.newPassword.trim();
    const confirmPassword = passwordForm.confirmPassword.trim();

    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError("All password fields are required.");
      return;
    }

    if (newPassword.length < 8) {
      setPasswordError("New password must be at least 8 characters long.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError("New password and confirmation do not match.");
      return;
    }

    if (currentPassword === newPassword) {
      setPasswordError("New password must be different from your current password.");
      return;
    }

    setChangingPassword(true);

    try {
      await apiFetch("/api/users/me/password", {
        method: "PUT",
        body: JSON.stringify({ currentPassword, newPassword })
      });

      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setPasswordMessage("Password updated successfully.");
    } catch (err) {
      setPasswordError(err.message || "Failed to change password.");
    } finally {
      setChangingPassword(false);
    }
  }

  function openPasswordModal() {
    setPasswordError("");
    setPasswordMessage("");
    setShowPasswordModal(true);
  }

  function closePasswordModal() {
    if (changingPassword) return;
    setShowPasswordModal(false);
    setPasswordError("");
    setPasswordMessage("");
    setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
  }

  const [form, setForm] = useState({
    name: "",
    description: "",
    price: "",
    stock: "",
    category: ""
  });
  const [editingProduct, setEditingProduct] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [customCategories, setCustomCategories] = useState([]);
  const [creatingNewCategory, setCreatingNewCategory] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState("");

  const availableCategories = [...new Set([...products.map((product) => product.category).filter(Boolean), ...customCategories, form.category].filter(Boolean))]
    .sort((a, b) => a.localeCompare(b));

  function resetForm() {
    setForm({ name: "", description: "", price: "", stock: "", category: "" });
    setEditingProduct(null);
    setError("");
    setMessage("");
    setImageFile(null);
    setImagePreview(null);
    setCreatingNewCategory(false);
    setNewCategoryName("");
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
    setCreatingNewCategory(false);
    setNewCategoryName("");
    setError("");
    setMessage("");
    setImageFile(null);
    setImagePreview(null);
  }

  function handleAddCategory() {
    const categoryToAdd = newCategoryName.trim();

    if (!categoryToAdd) {
      setError("Category name is required.");
      return;
    }

    setCustomCategories((current) => (current.includes(categoryToAdd) ? current : [...current, categoryToAdd]));
    handleInputChange("category", categoryToAdd);
    setNewCategoryName("");
    setCreatingNewCategory(false);
  }

  async function handleDeleteImage() {
    if (!editingProduct) return;
    setSaving(true);
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${BASE_URL}/api/products/${editingProduct.id}/image`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("Failed to remove image");
      setImagePreview(null);
      setImageFile(null);
      setMessage("Image removed.");
    } catch (err) {
      setError(err.message || "Failed to remove image.");
    } finally {
      setSaving(false);
    }
  }

  function selectOrder(order) {
    setOrders([order]);
    setSelectedOrder(order);
    setOrderDetails(order);
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
      let savedId;
      if (editingProduct) {
        await apiFetch(`/api/products/${editingProduct.id}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
        savedId = editingProduct.id;
      } else {
        const created = await apiFetch("/api/products", {
          method: "POST",
          body: JSON.stringify(payload)
        });
        savedId = created.id;
      }

      if (imageFile && savedId) {
        const formData = new FormData();
        formData.append("file", imageFile);
        const token = localStorage.getItem("token");
        await fetch(`${BASE_URL}/api/products/${savedId}/image`, {
          method: "PUT",
          headers: { Authorization: `Bearer ${token}` },
          body: formData
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
    if (!selectedOrder?.id) {
      setError("Order number is required.");
      return;
    }

    const targetOrderId = selectedOrder.id;

    setSaving(true);
    setError("");
    setMessage("");

    try {
      await apiFetch(`/api/order/${targetOrderId}/status?status=${encodeURIComponent(orderStatus)}`, {
        method: "PUT"
      });
      await loadOrderById(String(targetOrderId));
      await loadAllOrders();
      await loadPendingOrdersCount();
      setMessage("Order status updated successfully.");
    } catch (err) {
      setError(err.message || "Unable to update order status.");
    } finally {
      setSaving(false);
    }
  }

  async function cancelOrder() {
    if (!selectedOrder?.id) {
      setError("Order number is required.");
      return;
    }

    const targetOrderId = selectedOrder.id;

    setSaving(true);
    setError("");
    setMessage("");

    try {
      await apiFetch(`/api/order/${targetOrderId}/cancel`, {
        method: "PUT"
      });
      await loadOrderById(String(targetOrderId));
      await loadAllOrders();
      await loadPendingOrdersCount();
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
  const totalOrders = allOrders.length;
  const pendingOrders = pendingOrdersCount;
  const totalRevenue = allOrders.filter(o => String(o.status || "").toUpperCase() !== "CANCELLED").reduce((sum, o) => sum + (o.totalPrice || 0), 0);

  const filteredAllOrders = allOrdersFilter === "ALL"
    ? allOrders
    : allOrders.filter(order => String(order.status || "").toUpperCase() === allOrdersFilter);

  const orderNumberById = new Map(allOrders.map((order, index) => [String(order.id), index + 1]));
  const getDisplayOrderNumber = (order) => orderNumberById.get(String(order?.id)) ?? null;
  const getOrderDetailsCode = (order) => {
    const customerNumber = Number.parseInt(String(order?.userId ?? "0"), 10) || 0;
    const priceDigits = Math.max(0, Math.round(Number(order?.totalPrice ?? 0) * 100));
    const orderNumber = getDisplayOrderNumber(order) ?? 0;
    const combinedDigits = `${customerNumber}${priceDigits}${orderNumber}`;

    return `#${combinedDigits.padStart(7, "0")}`;
  };

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
      <p style={{ textAlign: "center", marginTop: "-20px", marginBottom: "30px" }}>
        <button
          type="button"
          onClick={openPasswordModal}
          style={{ background: "none", border: "none", padding: 0, color: theme.buttonPrimary, textDecoration: "underline", cursor: "pointer", fontSize: "14px", fontWeight: "600" }}
        >
          Change password
        </button>
      </p>

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
          <p style={{ color: theme.success, fontSize: "24px", fontWeight: "bold", margin: 0 }}>€{totalRevenue.toFixed(2)}</p>
        </div>
      </div>

      {showPasswordModal && (
        <div style={{ position: "fixed", inset: 0, backgroundColor: "rgba(0, 0, 0, 0.45)", display: "flex", justifyContent: "center", alignItems: "center", zIndex: 1000, padding: "20px" }}>
          <section style={{ width: "100%", maxWidth: "560px", backgroundColor: theme.backgroundWhite, padding: "24px", borderRadius: "10px", border: `1px solid ${theme.border}`, boxShadow: "0 10px 30px rgba(0,0,0,0.2)" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px" }}>
              <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: 0, fontSize: "22px" }}>Change Password</h3>
              <button
                type="button"
                onClick={closePasswordModal}
                disabled={changingPassword}
                style={{ padding: "8px 12px", backgroundColor: theme.textMuted, color: theme.buttonPrimaryText, border: "none", borderRadius: "6px", cursor: changingPassword ? "not-allowed" : "pointer" }}
              >
                Close
              </button>
            </div>

            {passwordError && (
              <p style={{ color: theme.errorText, backgroundColor: theme.errorBackground, padding: "12px", borderRadius: "8px", marginBottom: "14px" }}>
                {passwordError}
              </p>
            )}

            {passwordMessage && (
              <p style={{ color: theme.success, backgroundColor: theme.backgroundWarm, border: `1px solid ${theme.border}`, padding: "12px", borderRadius: "8px", marginBottom: "14px" }}>
                {passwordMessage}
              </p>
            )}

            <form onSubmit={handleChangePassword} style={{ display: "grid", gridTemplateColumns: "repeat(2, minmax(0, 1fr))", gap: "12px 16px", alignItems: "end" }}>
              <div style={{ gridColumn: "1 / -1" }}>
                <label style={{ display: "block", marginBottom: "6px", color: theme.textMuted, fontSize: "13px", fontWeight: "600" }}>Current Password</label>
                <input
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={(e) => handlePasswordInputChange("currentPassword", e.target.value)}
                  disabled={changingPassword}
                  autoComplete="current-password"
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: `1px solid ${theme.border}`, boxSizing: "border-box" }}
                />
              </div>

              <div>
                <label style={{ display: "block", marginBottom: "6px", color: theme.textMuted, fontSize: "13px", fontWeight: "600" }}>New Password</label>
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) => handlePasswordInputChange("newPassword", e.target.value)}
                  disabled={changingPassword}
                  autoComplete="new-password"
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: `1px solid ${theme.border}`, boxSizing: "border-box" }}
                />
              </div>

              <div>
                <label style={{ display: "block", marginBottom: "6px", color: theme.textMuted, fontSize: "13px", fontWeight: "600" }}>Confirm New Password</label>
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => handlePasswordInputChange("confirmPassword", e.target.value)}
                  disabled={changingPassword}
                  autoComplete="new-password"
                  style={{ width: "100%", padding: "10px", borderRadius: "6px", border: `1px solid ${theme.border}`, boxSizing: "border-box" }}
                />
              </div>

              <div style={{ gridColumn: "1 / -1", display: "flex", justifyContent: "flex-end" }}>
                <button
                  type="submit"
                  disabled={changingPassword}
                  style={{ padding: "10px 16px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: changingPassword ? "not-allowed" : "pointer" }}
                >
                  {changingPassword ? "Updating..." : "Update Password"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}

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
                  <select
                    value={creatingNewCategory ? "__new__" : form.category}
                    onChange={(e) => {
                      const selectedValue = e.target.value;

                      if (selectedValue === "__new__") {
                        setCreatingNewCategory(true);
                        handleInputChange("category", "");
                        return;
                      }

                      setCreatingNewCategory(false);
                      setNewCategoryName("");
                      handleInputChange("category", selectedValue);
                    }}
                    disabled={saving}
                    style={{ fontFamily: "'Arial', sans-serif", width: "100%", padding: "12px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "16px", transition: "border-color 0.2s" }}
                  >
                    <option value="">Select category</option>
                    {availableCategories.map((categoryOption) => (
                      <option key={categoryOption} value={categoryOption}>{categoryOption}</option>
                    ))}
                    <option value="__new__">+ Create New Category</option>
                  </select>

                  {creatingNewCategory && (
                    <div style={{ marginTop: "10px", display: "flex", gap: "8px" }}>
                      <input
                        type="text"
                        value={newCategoryName}
                        onChange={(e) => {
                          setNewCategoryName(e.target.value);
                          if (error) setError("");
                        }}
                        disabled={saving}
                        placeholder="Enter new category name"
                        style={{ fontFamily: "'Arial', sans-serif", flex: 1, padding: "10px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "14px" }}
                      />
                      <button
                        type="button"
                        onClick={handleAddCategory}
                        disabled={saving}
                        style={{ padding: "10px 12px", border: `1px solid ${theme.buttonPrimary}`, borderRadius: "6px", backgroundColor: theme.backgroundWhite, color: theme.buttonPrimary, cursor: saving ? "not-allowed" : "pointer", fontWeight: "600", fontSize: "13px" }}
                      >
                        Add
                      </button>
                    </div>
                  )}
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

              <div>
                <label style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, fontWeight: "600", display: "block", marginBottom: "8px", fontSize: "14px" }}>Product Image</label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => {
                    const file = e.target.files[0];
                    if (file) {
                      setImageFile(file);
                      setImagePreview(URL.createObjectURL(file));
                    }
                  }}
                  disabled={saving}
                  style={{ fontFamily: "'Arial', sans-serif", width: "100%", padding: "8px", border: `1px solid ${theme.border}`, boxSizing: "border-box", borderRadius: "6px", backgroundColor: theme.backgroundWhite, fontSize: "14px" }}
                />
                {(imagePreview || editingProduct) && (
                  <div style={{ marginTop: "10px", display: "flex", alignItems: "flex-start", gap: "10px" }}>
                    <img
                      src={imagePreview || `${BASE_URL}/api/products/${editingProduct?.id}/image`}
                      alt="Product preview"
                      onError={(e) => { e.target.style.display = "none"; }}
                      style={{ maxWidth: "120px", maxHeight: "120px", borderRadius: "6px", border: `1px solid ${theme.border}`, objectFit: "cover" }}
                    />
                    {editingProduct && !imageFile && (
                      <button
                        type="button"
                        onClick={handleDeleteImage}
                        disabled={saving}
                        style={{ padding: "6px 12px", backgroundColor: theme.errorBackground, color: theme.errorText, border: `1px solid ${theme.errorText}`, borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer", fontSize: "13px" }}
                      >
                        Remove Image
                      </button>
                    )}
                  </div>
                )}
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
        <div style={{ display: "grid", gridTemplateColumns: "minmax(280px, 340px) 1fr", gap: "20px", alignItems: "start" }}>
          <section style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
            <h3 style={{ color: theme.textPrimary, margin: "0 0 14px 0", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>Order Lookup</h3>
            <label style={{ color: theme.textPrimary, fontWeight: "500" }}>Order Number:</label>
            <input
              type="text"
              value={orderLookupNumber}
              onChange={e => {
                setOrderLookupNumber(e.target.value);
                if (error) setError("");
                if (message) setMessage("");
              }}
              disabled={saving}
              style={{ width: "100%", padding: "12px", marginTop: "5px", border: `1px solid ${theme.border}`, borderRadius: "4px", backgroundColor: theme.backgroundWhite, boxSizing: "border-box" }}
            />
            <button
              onClick={() => loadOrderByNumber(orderLookupNumber)}
              disabled={saving || loading}
              style={{ width: "100%", padding: "10px 16px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: saving || loading ? "not-allowed" : "pointer", marginTop: "10px" }}
            >
              {loading ? "Loading..." : "Find Order"}
            </button>
            <p style={{ color: theme.textMuted, margin: "12px 0 0 0", fontSize: "13px" }}>
              Search an order by number, then manage it on the right panel.
            </p>
          </section>

          <section style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
            <h3 style={{ color: theme.textPrimary, margin: "0 0 16px 0", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>Order Management</h3>

            {!selectedOrder && (
              <p style={{ color: theme.textMuted, marginTop: 0 }}>Search by order number to view and manage an order.</p>
            )}

            {selectedOrder && (
              <>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(170px, 1fr))", gap: "10px", marginBottom: "16px" }}>
                  <div style={{ border: `1px solid ${theme.border}`, borderRadius: "6px", padding: "10px" }}>
                    <p style={{ color: theme.textMuted, margin: "0 0 4px 0", fontSize: "12px" }}>Order Number</p>
                    <p style={{ color: theme.textPrimary, margin: 0, fontWeight: "600" }}>{getDisplayOrderNumber(selectedOrder) ?? "-"}</p>
                  </div>
                  <div style={{ border: `1px solid ${theme.border}`, borderRadius: "6px", padding: "10px" }}>
                    <p style={{ color: theme.textMuted, margin: "0 0 4px 0", fontSize: "12px" }}>User ID</p>
                    <p style={{ color: theme.textPrimary, margin: 0, fontWeight: "600" }}>{selectedOrder.userId}</p>
                  </div>
                  <div style={{ border: `1px solid ${theme.border}`, borderRadius: "6px", padding: "10px" }}>
                    <p style={{ color: theme.textMuted, margin: "0 0 4px 0", fontSize: "12px" }}>Total</p>
                    <p style={{ color: theme.textPrimary, margin: 0, fontWeight: "600" }}>€{selectedOrder.totalPrice?.toFixed(2)}</p>
                  </div>
                  <div style={{ border: `1px solid ${theme.border}`, borderRadius: "6px", padding: "10px" }}>
                    <p style={{ color: theme.textMuted, margin: "0 0 4px 0", fontSize: "12px" }}>Ordered Date</p>
                    <p style={{ color: theme.textPrimary, margin: 0, fontWeight: "600" }}>{new Date(selectedOrder.orderedDate).toLocaleDateString()}</p>
                  </div>
                </div>

                <div style={{ marginBottom: "14px" }}>
                  <label style={{ color: theme.textPrimary, fontWeight: "500", display: "block", marginBottom: "6px" }}>Status:</label>
                  <select
                    value={orderStatus}
                    onChange={e => setOrderStatus(e.target.value)}
                    disabled={saving}
                    style={{ width: "100%", padding: "12px", border: `1px solid ${theme.border}`, borderRadius: "4px", backgroundColor: theme.backgroundWhite, boxSizing: "border-box" }}
                  >
                    <option value="PENDING">PENDING</option>
                    <option value="PAID">PAID</option>
                    <option value="SHIPPED">SHIPPED</option>
                    <option value="DELIVERED">DELIVERED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>

                <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                  <button
                    onClick={updateOrderStatus}
                    disabled={saving}
                    style={{ padding: "10px 16px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer" }}
                  >
                    Update Status
                  </button>
                  <button
                    onClick={cancelOrder}
                    disabled={saving}
                    style={{ padding: "10px 16px", backgroundColor: theme.errorText, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: saving ? "not-allowed" : "pointer" }}
                  >
                    Cancel Order
                  </button>
                </div>
              </>
            )}

            <div style={{ marginTop: "24px", paddingTop: "18px", borderTop: `1px solid ${theme.border}` }}>
              <h4 style={{ color: theme.textPrimary, margin: "0 0 12px 0", borderBottom: `1px solid ${theme.border}`, paddingBottom: "8px" }}>Loaded Order List</h4>
              {!orders.length && (
                <p style={{ color: theme.textMuted, marginTop: 0 }}>No orders loaded yet.</p>
              )}
              {!!orders.length && (
                <div style={{ display: "grid", gap: "16px" }}>
                  {orders.map(order => (
                    <section key={order.id} style={{ border: `1px solid ${theme.border}`, borderRadius: "10px", padding: "18px", backgroundColor: theme.backgroundWarm }}>
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "12px", marginBottom: "16px", flexWrap: "wrap" }}>
                        <h5 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: 0, fontSize: "18px", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "8px" }}>
                          Order Details - {getOrderDetailsCode(order)}
                        </h5>
                        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                          <span style={{ color: theme.textMuted, fontSize: "14px", fontWeight: "600" }}>Status:</span>
                          {statusBadge(order.status)}
                        </div>
                      </div>

                      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: "8px 24px", marginBottom: "20px" }}>
                        <p style={{ color: theme.textPrimary, margin: "4px 0", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Order Number:</strong> {getDisplayOrderNumber(order) ?? "-"}</p>
                        <p style={{ color: theme.textPrimary, margin: "4px 0", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>User ID:</strong> {order.userId ?? "N/A"}</p>
                        <p style={{ color: theme.textPrimary, margin: "4px 0", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Date:</strong> {formatDate(order.orderedDate)}</p>
                        <p style={{ color: theme.textPrimary, margin: "4px 0", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Total:</strong> {formatCurrency(order.totalPrice)}</p>
                      </div>

                      {/* Shipping Address */}
                      <div style={{
                        marginBottom: "20px",
                        padding: "16px",
                        borderRadius: "8px",
                        border: `1px solid ${theme.border}`,
                        backgroundColor: theme.backgroundWhite
                      }}>
                        <h6 style={{
                          fontFamily: "'Georgia', serif",
                          color: theme.textPrimary,
                          margin: "0 0 12px 0",
                          fontSize: "16px",
                          borderBottom: `1px solid ${theme.border}`,
                          paddingBottom: "8px"
                        }}>
                          Shipping Address
                        </h6>
                      
                        <div style={{ display: "grid", gap: "4px" }}>
                          <p style={{ margin: 0, color: theme.textPrimary, fontWeight: "600" }}>
                            {order.fullName || "N/A"}
                          </p>
                      
                          <p style={{ margin: 0, color: theme.textPrimary }}>
                            {order.streetAddress || ""}
                          </p>
                      
                          {order.streetAddress2 && (
                            <p style={{ margin: 0, color: theme.textPrimary }}>
                              {order.streetAddress2}
                            </p>
                          )}
                      
                          <p style={{ margin: 0, color: theme.textPrimary }}>
                            {order.cityTown || ""}, {order.county || ""}
                          </p>
                        
                          <p style={{ margin: 0, color: theme.textMuted, fontSize: "13px" }}>
                            {order.eircode || ""}
                          </p>
                        </div>
                      </div>

                      <h6 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 12px 0", fontSize: "16px", paddingTop: "16px", borderTop: `1px solid ${theme.border}` }}>Items</h6>
                      {order.items?.length ? (
                        <div style={{ overflowX: "auto" }}>
                          <table style={{ width: "100%", borderCollapse: "collapse" }}>
                            <thead>
                              <tr>
                                <th style={{ textAlign: "left", padding: "10px 12px", borderBottom: `1px solid ${theme.border}`, color: theme.textMuted, fontSize: "12px", fontWeight: "600", letterSpacing: "0.06em", textTransform: "uppercase", backgroundColor: theme.backgroundWhite }}>Product</th>
                                <th style={{ textAlign: "right", padding: "10px 12px", borderBottom: `1px solid ${theme.border}`, color: theme.textMuted, fontSize: "12px", fontWeight: "600", letterSpacing: "0.06em", textTransform: "uppercase", backgroundColor: theme.backgroundWhite }}>Price</th>
                                <th style={{ textAlign: "right", padding: "10px 12px", borderBottom: `1px solid ${theme.border}`, color: theme.textMuted, fontSize: "12px", fontWeight: "600", letterSpacing: "0.06em", textTransform: "uppercase", backgroundColor: theme.backgroundWhite }}>Qty</th>
                                <th style={{ textAlign: "right", padding: "10px 12px", borderBottom: `1px solid ${theme.border}`, color: theme.textMuted, fontSize: "12px", fontWeight: "600", letterSpacing: "0.06em", textTransform: "uppercase", backgroundColor: theme.backgroundWhite }}>Total</th>
                              </tr>
                            </thead>
                            <tbody>
                              {order.items.map((item, index) => (
                                <tr key={`${order.id}-${index}`}>
                                  <td style={{ padding: "12px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary, fontSize: "14px" }}>{item.productName || item.name || "Unknown product"}</td>
                                  <td style={{ padding: "12px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary, fontSize: "14px", whiteSpace: "nowrap" }}>{formatCurrency(item.price)}</td>
                                  <td style={{ padding: "12px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary, fontSize: "14px" }}>{item.quantity ?? 0}</td>
                                  <td style={{ padding: "12px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary, fontSize: "14px", whiteSpace: "nowrap" }}>{formatCurrency(Number(item.price ?? 0) * Number(item.quantity ?? 0))}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      ) : (
                        <p style={{ color: theme.textMuted, margin: 0 }}>No items found for this order.</p>
                      )}
                    </section>
                  ))}
                </div>
              )}
            </div>
          </section>
        </div>

        <section style={{ marginTop: "20px", backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "14px", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px", gap: "10px", flexWrap: "wrap" }}>
            <h3 style={{ color: theme.textPrimary, margin: 0 }}>All Orders</h3>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <label style={{ color: theme.textPrimary, fontWeight: "500", fontSize: "14px" }}>Filter:</label>
              <select
                value={allOrdersFilter}
                onChange={e => setAllOrdersFilter(e.target.value)}
                style={{ padding: "8px", border: `1px solid ${theme.border}`, borderRadius: "4px", backgroundColor: theme.backgroundWhite }}
              >
                <option value="ALL">All</option>
                <option value="PAID">Paid</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="DELIVERED">Delivered</option>
                <option value="PENDING">Pending</option>
                <option value="SHIPPED">Shipped</option>
              </select>
              <button
                onClick={loadAllOrders}
                disabled={loading || saving}
                style={{ padding: "8px 12px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "4px", cursor: loading || saving ? "not-allowed" : "pointer" }}
              >
                Refresh
              </button>
            </div>
          </div>

          {!filteredAllOrders.length && (
            <p style={{ color: theme.textMuted, marginTop: 0 }}>No orders found.</p>
          )}

          {!!filteredAllOrders.length && (
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Order Number</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>User ID</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Status</th>
                    <th style={{ textAlign: "right", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Total</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Ordered Date</th>
                    <th style={{ textAlign: "left", padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredAllOrders.map(order => (
                    <tr key={order.id}>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{getDisplayOrderNumber(order) ?? "-"}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{order.userId}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{order.status}</td>
                      <td style={{ padding: "10px", textAlign: "right", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>€{order.totalPrice?.toFixed(2)}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary }}>{order.orderedDate ? new Date(order.orderedDate).toLocaleDateString() : "-"}</td>
                      <td style={{ padding: "10px", borderBottom: `1px solid ${theme.border}` }}>
                        <button
                          onClick={() => loadOrderById(String(order.id))}
                          style={{ padding: "6px 10px", borderRadius: "4px", border: `1px solid ${theme.buttonPrimary}`, backgroundColor: theme.backgroundWhite, color: theme.buttonPrimary, cursor: "pointer" }}
                        >
                          Select
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
        </>
      )}

    </div>
  );
}

export default AdminDashboard;