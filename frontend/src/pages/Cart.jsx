import { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import { apiFetch } from "../api/api.js";
import theme from "../styles/theme";

function Cart() {
  const { cart, removeFromCart, updateQuantity } = useContext(CartContext);
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(true);

  const total = cart.reduce(
    (sum, item) => sum + (Number(item.price) || 0) * (Number(item.quantity) || 0),
    0
  );

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) { setUser(null); setLoadingUser(false); return; }
    apiFetch("/api/users/me")
      .then(userData => setUser(userData))
      .catch(() => { localStorage.removeItem("token"); setUser(null); })
      .finally(() => setLoadingUser(false));
  }, []);


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

      <button
        onClick={() => navigate(-1)}
        style={{
          position: "fixed",
          top: "84px",
          left: "20px",
          background: theme.backgroundWhite,
          border: `1px solid ${theme.border}`,
          color: theme.textPrimary,
          padding: "10px 14px",
          fontSize: "12px",
          letterSpacing: "0.08em",
          textTransform: "uppercase",
          cursor: "pointer",
          borderRadius: "2px",
          zIndex: 90,
        }}
      >
        ← Back
      </button>

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
            {cart.map(item => {
              const stock = item.stockQuantity ?? Infinity;
                        
              return (
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
                      €{Number(item.price).toFixed(2)} each
                      {item.quantity >= stock && (
                        <span style={{ color: theme.textAccent, marginLeft: "8px" }}>
                          (max stock reached)
                        </span>
                      )}
                    </p>
                  </div>
                    
                  <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
                    <span style={{
                      fontFamily: "'Georgia', serif",
                      fontSize: "16px",
                      color: theme.textPrimary,
                    }}>
                      €{(Number(item.price) * (Number(item.quantity) || 0)).toFixed(2)}
                    </span>
                  
                    <div style={{
                      display: "flex",
                      alignItems: "center",
                      border: `1px solid ${theme.border}`,
                      borderRadius: "2px",
                    }}>
                      <button
                        onClick={() => updateQuantity(item.id, item.quantity - 1)}
                        style={{
                          background: "none", border: "none", padding: "4px 10px",
                          cursor: "pointer", color: theme.textMuted,
                          fontSize: "16px", lineHeight: 1,
                        }}
                      >
                        −
                      </button>
                      <input
                        type="number"
                        value={item.quantity}
                        min="1"
                        max={stock}
                        onChange={e => {
                          const val = parseInt(e.target.value);
                          if (!isNaN(val) && val >= 1) updateQuantity(item.id, Math.min(val, stock));
                        }}
                        onBlur={e => {
                          if (e.target.value === "" || Number(e.target.value) < 1)
                            updateQuantity(item.id, 1);
                        }}
                        style={{
                          width: "36px", border: "none",
                          borderLeft: `1px solid ${theme.border}`,
                          borderRight: `1px solid ${theme.border}`,
                          textAlign: "center", fontSize: "13px",
                          color: theme.textPrimary, padding: "4px 0",
                          outline: "none", MozAppearance: "textfield",
                        }}
                      />
                      <button
                        onClick={() => updateQuantity(item.id, Math.min(item.quantity + 1, stock))}
                        disabled={item.quantity >= stock}
                        style={{
                          background: "none", border: "none", padding: "4px 10px",
                          cursor: item.quantity >= stock ? "not-allowed" : "pointer",
                          color: item.quantity >= stock ? theme.border : theme.textMuted,
                          fontSize: "16px", lineHeight: 1,
                        }}
                      >
                        +
                      </button>
                    </div>
                      
                    <button
                      onClick={() => removeFromCart(item.id)}
                      style={{
                        background: "none", border: "none",
                        color: theme.textMuted, cursor: "pointer",
                        fontSize: "18px", lineHeight: 1, padding: "4px",
                      }}
                    >
                      ×
                    </button>
                  </div>
                </div>
              );
            })}
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

          {loadingUser ? (
            <p style={{ color: theme.textMuted, marginBottom: "16px" }}>Checking login status...</p>
          ) : !user ? (
            <>
              <p style={{ color: theme.textMuted, marginBottom: "16px" }}>
                You must be logged in to checkout.
              </p>
              <button
                onClick={() => navigate("/login")}
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
                Login to Checkout
              </button>
            </>
          ) : (
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
          )}
        </>
      )}
    </div>
  );
}

export default Cart;