import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";
import theme from "../styles/theme";

function formatCurrency(value) {
  return `€${Number(value ?? 0).toFixed(2)}`;
}

function formatDate(value) {
  if (!value) {
    return "N/A";
  }

  return new Date(value).toLocaleDateString();
}

function Dashboard() {
  const [user, setUser] = useState(null);
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshingOrders, setRefreshingOrders] = useState(false);
  const [error, setError] = useState("");

  const loadOrders = async (userId, showSpinner = false) => {
    if (showSpinner) {
      setRefreshingOrders(true);
    }

    try {
      const data = await apiFetch(`/api/order/user/${userId}`);
      setOrders(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Failed to load orders");
      setOrders([]);
    } finally {
      if (showSpinner) {
        setRefreshingOrders(false);
      }
    }
  };

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError("");

        const userData = await apiFetch("/api/users/me");
        setUser(userData);
        await loadOrders(userData.userId);
      } catch (err) {
        setError(err.message || "Failed to load dashboard");
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  const pendingOrders = orders.filter(order => order.status === "PENDING").length;
  const latestOrder = orders.length > 0 ? orders[0] : null;
  const totalSpent = orders.reduce((sum, order) => sum + Number(order.totalPrice ?? 0), 0);

  const statusBadge = (status) => {
    const colours = {
      PENDING:   { bg: "#fff8e1", text: "#b8860b" },
      SHIPPED:   { bg: "#e8f0fe", text: "#1a56db" },
      DELIVERED: { bg: "#e6f4ea", text: "#2e7d32" },
      CANCELLED: { bg: theme.errorBackground, text: theme.errorText },
    };
    const c = colours[status] || { bg: theme.backgroundWarm, text: theme.textMuted };
    return (
      <span style={{ display: "inline-block", padding: "3px 10px", borderRadius: "12px", fontSize: "12px", fontWeight: "600", letterSpacing: "0.04em", backgroundColor: c.bg, color: c.text }}>
        {status}
      </span>
    );
  };

  const sectionHeading = (label) => (
    <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, marginTop: 0, marginBottom: "18px", fontSize: "20px", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>
      {label}
    </h3>
  );

  const card = { backgroundColor: theme.backgroundWhite, padding: "24px 30px", borderRadius: "12px", border: `1px solid ${theme.border}`, boxShadow: "0 2px 8px rgba(0,0,0,0.06)", marginBottom: "24px" };
  const thStyle = { textAlign: "left", padding: "10px 12px", borderBottom: `1px solid ${theme.border}`, color: theme.textMuted, fontFamily: "'Arial', sans-serif", fontSize: "12px", fontWeight: "600", letterSpacing: "0.06em", textTransform: "uppercase", backgroundColor: theme.backgroundWarm };
  const tdStyle = { padding: "12px", borderBottom: `1px solid ${theme.border}`, color: theme.textPrimary, fontFamily: "'Arial', sans-serif", fontSize: "14px" };

  if (loading) {
    return <p style={{ color: theme.textMuted, textAlign: "center", padding: "50px" }}>Loading dashboard...</p>;
  }

  if (!user) {
    return <p style={{ color: theme.textMuted, textAlign: "center", padding: "50px" }}>Please log in to view your dashboard.</p>;
  }

  return (
    <div style={{ backgroundColor: theme.backgroundWarm, minHeight: "100vh", padding: "30px 20px" }}>
      <div style={{ maxWidth: "1100px", margin: "0 auto" }}>

        <h2 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, fontSize: "30px", marginBottom: "6px", textAlign: "center" }}>Customer Dashboard</h2>
        <p style={{ color: theme.textMuted, textAlign: "center", marginBottom: "36px", fontFamily: "'Arial', sans-serif" }}>Welcome back, {user.email}</p>

        {error && (
          <p style={{ color: theme.errorText, backgroundColor: theme.errorBackground, padding: "15px", borderRadius: "8px", marginBottom: "24px", textAlign: "center", fontFamily: "'Arial', sans-serif" }}>
            {error}
          </p>
        )}

        {/* Stats Cards */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "20px", marginBottom: "30px" }}>
          {[
            { label: "Total Orders",   value: orders.length,              colour: theme.textAccent },
            { label: "Pending Orders", value: pendingOrders,              colour: theme.textAccent },
            { label: "Total Spent",    value: formatCurrency(totalSpent), colour: theme.success    },
          ].map(({ label, value, colour }) => (
            <div key={label} style={{ backgroundColor: theme.backgroundWhite, padding: "20px", borderRadius: "12px", border: `1px solid ${theme.border}`, boxShadow: "0 2px 8px rgba(0,0,0,0.06)", textAlign: "center" }}>
              <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 10px 0", fontSize: "16px" }}>{label}</h3>
              <p style={{ color: colour, fontSize: "26px", fontWeight: "bold", margin: 0, fontFamily: "'Arial', sans-serif" }}>{value}</p>
            </div>
          ))}
        </div>

        {/* Profile */}
        <section style={card}>
          {sectionHeading("Profile")}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "8px 24px" }}>
            {[
              ["Email",        user.email],
              ["User ID",      user.userId],
              ["Role",         user.role],
              ["Member Since", formatDate(user.createdAt)],
            ].map(([label, val]) => (
              <p key={label} style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}>
                <strong style={{ color: theme.textMuted, fontWeight: "600", marginRight: "6px" }}>{label}:</strong>{val}
              </p>
            ))}
          </div>
        </section>

        {/* Latest Order */}
        {latestOrder && (
          <section style={card}>
            {sectionHeading("Latest Order")}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: "8px 24px", marginBottom: "18px" }}>
              <p style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Order Number:</strong> {latestOrder.orderNumber}</p>
              <p style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Date:</strong> {formatDate(latestOrder.orderedDate)}</p>
              <p style={{ margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px", display: "flex", alignItems: "center", gap: "8px" }}><strong style={{ color: theme.textMuted }}>Status:</strong> {statusBadge(latestOrder.status)}</p>
              <p style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Total:</strong> {formatCurrency(latestOrder.totalPrice)}</p>
            </div>
            <button
              onClick={() => setSelectedOrder(latestOrder)}
              style={{ padding: "10px 20px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "6px", cursor: "pointer", fontFamily: "'Arial', sans-serif", fontWeight: "500", fontSize: "14px" }}
            >
              View Details
            </button>
          </section>
        )}

        {/* Order History */}
        <section style={card}>
          <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 18px 0", fontSize: "20px", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px" }}>Order History</h3>

          {orders.length === 0 ? (
            <p style={{ color: theme.textMuted, margin: 0, fontFamily: "'Arial', sans-serif" }}>You have no orders yet.</p>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr>
                    <th style={thStyle}>Order ID</th>
                    <th style={thStyle}>Order Number</th>
                    <th style={thStyle}>Date</th>
                    <th style={thStyle}>Status</th>
                    <th style={{ ...thStyle, textAlign: "right" }}>Total</th>
                    <th style={thStyle}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map(order => (
                    <tr key={order.id}>
                      <td style={tdStyle}>{order.id}</td>
                      <td style={tdStyle}>{order.orderNumber}</td>
                      <td style={tdStyle}>{formatDate(order.orderedDate)}</td>
                      <td style={tdStyle}>{statusBadge(order.status)}</td>
                      <td style={{ ...tdStyle, textAlign: "right" }}>{formatCurrency(order.totalPrice)}</td>
                      <td style={tdStyle}>
                        <button
                          onClick={() => setSelectedOrder(order)}
                          style={{ padding: "6px 12px", borderRadius: "4px", border: `1px solid ${theme.buttonPrimary}`, backgroundColor: theme.backgroundWhite, color: theme.buttonPrimary, cursor: "pointer", fontFamily: "'Arial', sans-serif", fontSize: "13px", fontWeight: "500" }}
                        >
                          View Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "16px" }}>
            <button
              onClick={() => loadOrders(user.userId, true)}
              disabled={refreshingOrders}
              style={{ padding: "8px 16px", backgroundColor: theme.buttonPrimary, color: theme.buttonPrimaryText, border: "none", borderRadius: "6px", cursor: refreshingOrders ? "not-allowed" : "pointer", fontFamily: "'Arial', sans-serif", fontWeight: "500", fontSize: "14px" }}
            >
              {refreshingOrders ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </section>

        {/* Selected Order Details */}
        {selectedOrder && (
          <section style={card}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "18px" }}>
              <h3 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: 0, fontSize: "20px", borderBottom: `2px solid ${theme.textAccent}`, paddingBottom: "10px", flex: 1, marginRight: "20px" }}>
                Order Details — {selectedOrder.orderNumber}
              </h3>
              <button
                onClick={() => setSelectedOrder(null)}
                style={{ padding: "8px 16px", backgroundColor: theme.textMuted, color: theme.buttonPrimaryText, border: "none", borderRadius: "6px", cursor: "pointer", fontFamily: "'Arial', sans-serif", fontWeight: "500", fontSize: "14px", flexShrink: 0 }}
              >
                Close
              </button>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: "8px 24px", marginBottom: "24px" }}>
              <p style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Order ID:</strong> {selectedOrder.id}</p>
              <p style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Date:</strong> {formatDate(selectedOrder.orderedDate)}</p>
              <p style={{ margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px", display: "flex", alignItems: "center", gap: "8px" }}><strong style={{ color: theme.textMuted }}>Status:</strong> {statusBadge(selectedOrder.status)}</p>
              <p style={{ color: theme.textPrimary, margin: "4px 0", fontFamily: "'Arial', sans-serif", fontSize: "14px" }}><strong style={{ color: theme.textMuted }}>Total:</strong> {formatCurrency(selectedOrder.totalPrice)}</p>
            </div>

            <h4 style={{ fontFamily: "'Georgia', serif", color: theme.textPrimary, margin: "0 0 12px 0", fontSize: "16px", paddingTop: "16px", borderTop: `1px solid ${theme.border}` }}>Items</h4>
            {selectedOrder.items?.length ? (
              <div style={{ overflowX: "auto" }}>
                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                  <thead>
                    <tr>
                      <th style={thStyle}>Product</th>
                      <th style={{ ...thStyle, textAlign: "right" }}>Price</th>
                      <th style={{ ...thStyle, textAlign: "right" }}>Qty</th>
                      <th style={{ ...thStyle, textAlign: "right" }}>Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedOrder.items.map((item, index) => (
                      <tr key={`${selectedOrder.id}-${index}`}>
                        <td style={tdStyle}>{item.productName}</td>
                        <td style={{ ...tdStyle, textAlign: "right" }}>{formatCurrency(item.price)}</td>
                        <td style={{ ...tdStyle, textAlign: "right" }}>{item.quantity}</td>
                        <td style={{ ...tdStyle, textAlign: "right" }}>{formatCurrency(Number(item.price ?? 0) * Number(item.quantity ?? 0))}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p style={{ color: theme.textMuted, margin: 0, fontFamily: "'Arial', sans-serif" }}>No items found for this order.</p>
            )}
          </section>
        )}

      </div>
    </div>
  );
}

export default Dashboard;