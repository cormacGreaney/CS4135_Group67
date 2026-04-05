import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import { apiFetch } from "../api/api";

function Checkout() {
  const { cart, clearCart } = useContext(CartContext);
  const [status, setStatus] = useState("");
  const navigate = useNavigate();

  const total = cart.reduce(
    (sum, item) => sum + (Number(item.price) || 0) * (Number(item.quantity) || 0),
    0
  );

  async function handleCheckout() {
    try {
      const user = await apiFetch("/api/users/me");

      const order = await apiFetch("/api/order", {
        method: "POST",
        body: JSON.stringify({
          userId: user.userId,
          items: cart.map(item => ({
            productId: item.id,
            productName: item.name,
            price: item.price,
            quantity: item.quantity,
          })),
        }),
      });

      const payment = await apiFetch("/api/payments", {
        method: "POST",
        body: JSON.stringify({
          orderId: order.id,
          amount: order.totalPrice,
          provider: "demo",
        }),
      });

      clearCart();
      setStatus(`Payment ${payment.status}`);
    } catch (e) {
      setStatus(e.message);
    }
  }

  return (
    <div style={{ padding: "20px" }}>
      <h2>Checkout</h2>

      {cart.length === 0 && !status ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          {!status && (
            <>
              {cart.map(item => (
                <div key={item.id} style={{ marginBottom: "8px" }}>
                  {item.name} x {item.quantity} — €{(Number(item.price) * Number(item.quantity)).toFixed(2)}
                </div>
              ))}

              <h3>Total: €{total.toFixed(2)}</h3>

              <button onClick={handleCheckout}>
                Pay Now
              </button>
            </>
          )}

          {status && (
            <div>
              <p>{status}</p>
              <button onClick={() => navigate("/")}>Back to shop</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default Checkout;