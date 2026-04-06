import { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import theme from "../styles/theme";

function Cart() {
  const { cart, removeFromCart } = useContext(CartContext);
  const navigate = useNavigate();

  const total = cart.reduce(
    (sum, item) => sum + (Number(item.price) || 0) * (Number(item.quantity) || 0),
    0
  );

  return (
    <div style={{ maxWidth: "720px", margin: "0 auto", padding: "48px 40px" }}>
      <p style={{
        fontSize: "11px",
        letterSpacing: "0.15em",
        textTransform: "uppercase",
        color: theme.textAccent,
        marginBottom: "8px",
      }}>
        Your Order
      </p>
      <h1 style={{
        fontFamily: "'Georgia', serif",
        fontSize: "32px",
        fontWeight: "400",
        color: theme.textPrimary,
        margin: "0 0 40px",
      }}>
        Cart
      </h1>

      {cart.length === 0 ? (
        <div style={{
          textAlign: "center",
          padding: "64px 0",
          borderTop: `1px solid ${theme.border}`,
          borderBottom: `1px solid ${theme.border}`,
        }}>
          <p style={{ color: theme.textMuted, marginBottom: "24px" }}>Your cart is empty.</p>
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
            Browse Products
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
                padding: "20px 0",
                borderBottom: `1px solid ${theme.border}`,
              }}>
                <div>
                  <p style={{
                    margin: "0 0 4px",
                    fontFamily: "'Georgia', serif",
                    fontSize: "16px",
                    color: theme.textPrimary,
                  }}>
                    {item.name}
                  </p>
                  <p style={{ margin: 0, fontSize: "13px", color: theme.textMuted }}>
                    Qty: {item.quantity} · €{Number(item.price).toFixed(2)} each
                  </p>
                </div>

                <div style={{ display: "flex", alignItems: "center", gap: "24px" }}>
                  <span style={{
                    fontFamily: "'Georgia', serif",
                    fontSize: "16px",
                    color: theme.textPrimary,
                  }}>
                    €{(Number(item.price) * item.quantity).toFixed(2)}
                  </span>
                  <button
                    onClick={() => removeFromCart(item.id)}
                    style={{
                      background: "none",
                      border: "none",
                      color: theme.textMuted,
                      cursor: "pointer",
                      fontSize: "18px",
                      lineHeight: 1,
                      padding: "4px",
                    }}
                  >
                    ×
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "24px 0",
            marginBottom: "24px",
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

          <button
            onClick={() => navigate("/checkout")}
            style={{
              width: "100%",
              background: theme.buttonPrimary,
              color: theme.buttonPrimaryText,
              border: "none",
              padding: "16px",
              fontSize: "12px",
              letterSpacing: "0.12em",
              textTransform: "uppercase",
              cursor: "pointer",
              borderRadius: "2px",
            }}
          >
            Proceed to Checkout
          </button>
        </>
      )}
    </div>
  );
}

export default Cart;