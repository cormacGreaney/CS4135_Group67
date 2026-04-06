import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";

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

  if (loading) {
    return <p style={{ padding: "20px" }}>Loading dashboard...</p>;
  }

  if (!user) {
    return <p style={{ padding: "20px" }}>Please log in to view your dashboard.</p>;
  }

  return (
    <div style={{ padding: "20px" }}>
      <div>
        <h1>Customer Dashboard</h1>
        <p>Welcome, {user.email}</p>
      </div>

      {error && (
        <p>{error}</p>
      )}

      <hr />

      <section>
        <h2>Profile</h2>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>User ID:</strong> {user.userId}</p>
        <p><strong>Role:</strong> {user.role}</p>
        <p><strong>Member Since:</strong> {formatDate(user.createdAt)}</p>
      </section>

      <hr />

      <section>
        <h2>Order Summary</h2>
        <p><strong>Total Orders:</strong> {orders.length}</p>
        <p><strong>Pending Orders:</strong> {pendingOrders}</p>
        <p><strong>Total Spent:</strong> {formatCurrency(totalSpent)}</p>
        {latestOrder ? (
          <>
            <p><strong>Latest Order Number:</strong> {latestOrder.orderNumber}</p>
            <p><strong>Latest Order Date:</strong> {formatDate(latestOrder.orderedDate)}</p>
            <p><strong>Latest Order Status:</strong> {latestOrder.status}</p>
            <button onClick={() => setSelectedOrder(latestOrder)}>
              Show Latest Order Details
            </button>
          </>
        ) : (
          <p>No orders available yet.</p>
        )}
      </section>

      <hr />

      <section>
        <h2>Order History</h2>
        <button onClick={() => loadOrders(user.userId, true)} disabled={refreshingOrders}>
          {refreshingOrders ? "Refreshing..." : "Refresh Orders"}
        </button>

        {orders.length === 0 ? (
          <p>You have no orders yet.</p>
        ) : (
          <table border="1" cellPadding="8" cellSpacing="0" style={{ width: "100%", marginTop: "12px" }}>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Order Number</th>
                <th>Date</th>
                <th>Status</th>
                <th>Total</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.orderNumber}</td>
                  <td>{formatDate(order.orderedDate)}</td>
                  <td>{order.status}</td>
                  <td>{formatCurrency(order.totalPrice)}</td>
                  <td>
                    <button onClick={() => setSelectedOrder(order)}>
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {selectedOrder && (
        <>
          <hr />

          <section>
            <h2>Selected Order Details</h2>
            <button onClick={() => setSelectedOrder(null)}>
              Clear Selected Order
            </button>
            <p><strong>Order ID:</strong> {selectedOrder.id}</p>
            <p><strong>Order Number:</strong> {selectedOrder.orderNumber}</p>
            <p><strong>Date:</strong> {formatDate(selectedOrder.orderedDate)}</p>
            <p><strong>Status:</strong> {selectedOrder.status}</p>
            <p><strong>Total Amount:</strong> {formatCurrency(selectedOrder.totalPrice)}</p>

            <h3>Items</h3>
            {selectedOrder.items?.length ? (
              <table border="1" cellPadding="8" cellSpacing="0" style={{ width: "100%" }}>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Total</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedOrder.items.map((item, index) => (
                    <tr key={`${selectedOrder.id}-${index}`}>
                      <td>{item.productName}</td>
                      <td>{formatCurrency(item.price)}</td>
                      <td>{item.quantity}</td>
                      <td>{formatCurrency(Number(item.price ?? 0) * Number(item.quantity ?? 0))}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>No items found for this order.</p>
            )}
          </section>
        </>
      )}
    </div>
  );
}

export default Dashboard;