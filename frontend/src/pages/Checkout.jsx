import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import { apiFetch } from "../api/api";
import theme from "../styles/theme";

function Checkout() {
  const { cart, clearCart } = useContext(CartContext);
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const total = cart.reduce(
    (sum, item) => sum + (Number(item.price) || 0) * (Number(item.quantity) || 0),
    0
  );

  async function handleCheckout() {
    setLoading(true);
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
      setStatus(payment.status);
    } catch (e) {
      setStatus(`error: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }

  const success = status && !status.startsWith("error");

  return (
    <div style={{ maxWidth: "720px", margin: "0 auto", padding: "48px 40px" }}>
      <p style={{
        fontSize: "11px",
        letterSpacing: "0.15em",
        textTransform: "uppercase",
        color: theme.textAccent,
        marginBottom: "8px",
      }}>
        Almost there
      </p>
      <h1 style={{
        fontFamily: "'Georgia', serif",
        fontSize: "32px",
        fontWeight: "400",
        color: theme.textPrimary,
        margin: "0 0 40px",
      }}>
        Checkout
      </h1>

      {success ? (
        <div style={{
          textAlign: "center",
          padding: "64px 0",
          borderTop: `1px solid ${theme.border}`,
          borderBottom: `1px solid ${theme.border}`,
        }}>
          <p style={{
            fontFamily: "'Georgia', serif",
            fontSize: "22px",
            color: theme.textPrimary,
            marginBottom: "8px",
          }}>
            Order confirmed
          </p>
          <p style={{ color: theme.textMuted, fontSize: "14px", marginBottom: "32px" }}>
            Payment {status.toLowerCase()}. Thank you for your order.
          </p>
          <button
            onClick={() => navigate("/")}
            style={{
              background: theme.buttonPrimary,
              color: theme.buttonPrimaryText,
              border: "none",
              padding: "12px 24px",
              fontSize: "11px",
              letterSpacing: "0.1em",
              textTransform: "uppercase",
              cursor: "pointer",
              borderRadius: "2px",
            }}
          >
            Back to shop
          </button>
        </div>
      ) : (
        <>
          <div style={{ borderTop: `1px solid ${theme.border}` }}>
            {cart.map(item => (
              <div key={item.id} style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "16px 0",
                borderBottom: `1px solid ${theme.border}`,
              }}>
                <div>
                  <p style={{
                    margin: "0 0 2px",
                    fontFamily: "'Georgia', serif",
                    fontSize: "15px",
                    color: theme.textPrimary,
                  }}>
                    {item.name}
                  </p>
                  <p style={{ margin: 0, fontSize: "13px", color: theme.textMuted }}>
                    Qty: {item.quantity}
                  </p>
                </div>
                <span style={{
                  fontFamily: "'Georgia', serif",
                  fontSize: "15px",
                  color: theme.textPrimary,
                }}>
                  €{(Number(item.price) * item.quantity).toFixed(2)}
                </span>
              </div>
            ))}
          </div>

          <div style={{
            display: "flex",
            justifyContent: "space-between",
            padding: "24px 0",
            marginBottom: "32px",
          }}>
            <span style={{
              fontSize: "11px",
              letterSpacing: "0.1em",
              textTransform: "uppercase",
              color: theme.textMuted,
            }}>
              Total
            </span>
            <span style={{
              fontFamily: "'Georgia', serif",
              fontSize: "24px",
              color: theme.textPrimary,
            }}>
              €{total.toFixed(2)}
            </span>
          </div>

          {status.startsWith("error") && (
            <p style={{
              fontSize: "13px",
              color: theme.errorText,
              marginBottom: "16px",
              padding: "12px 16px",
              background: theme.errorBackground,
              borderRadius: "2px",
            }}>
              {status.replace("error: ", "")}
            </p>
          )}

          <button
            onClick={handleCheckout}
            disabled={cart.length === 0 || loading}
            style={{
              width: "100%",
              background: loading ? theme.textMuted : theme.buttonPrimary,
              color: theme.buttonPrimaryText,
              border: "none",
              padding: "16px",
              fontSize: "12px",
              letterSpacing: "0.12em",
              textTransform: "uppercase",
              cursor: loading ? "not-allowed" : "pointer",
              borderRadius: "2px",
              transition: "background 0.2s",
            }}
          >
            {loading ? "Processing..." : "Pay Now"}
          </button>
        </>
      )}
    </div>
  );
}

export default Checkout;