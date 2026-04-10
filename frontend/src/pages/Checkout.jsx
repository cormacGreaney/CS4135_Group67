import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CartContext } from "../context/CartContext";
import { apiFetch } from "../api/api";
import theme from "../styles/theme";

const inputStyle = {
  width: "100%",
  padding: "10px 12px",
  border: `1px solid ${theme.border}`,
  borderRadius: "2px",
  fontSize: "14px",
  color: theme.textPrimary,
  background: theme.backgroundWhite,
  outline: "none",
  boxSizing: "border-box",
};

const labelStyle = {
  display: "block",
  fontSize: "11px",
  letterSpacing: "0.1em",
  textTransform: "uppercase",
  color: theme.textMuted,
  marginBottom: "6px",
};

function Checkout() {
  const { cart, clearCart } = useContext(CartContext);
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const [shipping, setShipping] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    county: "",
    eircode: "",
  });

  const [card, setCard] = useState({
    number: "",
    name: "",
    expiry: "",
    cvv: "",
  });

  const [shippingError, setShippingError] = useState("");
  const [cardError, setCardError] = useState("");

  const total = cart.reduce(
    (sum, item) => sum + (Number(item.price) || 0) * (Number(item.quantity) || 0),
    0
  );

  function validateShipping() {
    if (!shipping.fullName.trim()) return "Full name is required.";
    if (!shipping.addressLine1.trim()) return "Address is required.";
    if (!shipping.city.trim()) return "City is required.";
    if (!shipping.eircode.trim()) return "Eircode is required.";
    return null;
  }

  function validateCard() {
    const digits = card.number.replace(/\s/g, "");
    if (digits.length < 12 || digits.length > 19 || isNaN(Number(digits))) return "Enter a valid card number.";
    if (!card.name.trim()) return "Name on card is required.";
    if (!/^\d{2}\/\d{2}$/.test(card.expiry)) return "Expiry must be MM/YY.";
    if (card.cvv.length < 3) return "CVV must be at least 3 digits.";
    return null;
  }

  function formatCardNumber(val) {
    return val.replace(/\D/g, "").slice(0, 19).replace(/(.{4})/g, "$1 ").trim();
  }

  function formatExpiry(val) {
    const digits = val.replace(/\D/g, "").slice(0, 4);
    if (digits.length >= 3) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    return digits;
  }

  async function handleCheckout() {
    setShippingError("");
    setCardError("");

    const sErr = validateShipping();
    if (sErr) { setShippingError(sErr); return; }

    const cErr = validateCard();
    if (cErr) { setCardError(cErr); return; }

    setLoading(true);
    try {
      const user = await apiFetch("/api/users/me");

      const order = await apiFetch("/api/order", {
        method: "POST",
        body: JSON.stringify({
          userId: user.userId,
        
          fullName: shipping.fullName,
          streetAddress: shipping.addressLine1,
          streetAddress2: shipping.addressLine2 || "",
          cityTown: shipping.city,
          county: shipping.county,
          eircode: shipping.eircode,
        
          items: cart.map(item => ({
            productId: item.id,
            productName: item.name,
            price: item.price,
            quantity: item.quantity,
          })),
        }),
      });

      const [expiryMonth, expiryYear] = card.expiry.split("/").map(Number);

      const payment = await apiFetch("/api/payments/checkout-card", {
        method: "POST",
        body: JSON.stringify({
          orderId: order.id,
          amount: order.totalPrice,
          cardNumber: card.number.replace(/\s/g, ""),
          cardHolderName: card.name,
          expiryMonth,
          expiryYear: expiryYear + 2000,
          cvv: card.cvv,
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

  const success = status === "SUCCESS";

  return (
    <div style={{ maxWidth: "720px", margin: "0 auto", padding: "48px 40px" }}>
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
            Payment successful. Thank you for your order.
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
          {/* Order summary */}
          <div style={{ borderTop: `1px solid ${theme.border}`, marginBottom: "40px" }}>
            {cart.map(item => (
              <div key={item.id} style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "16px 0",
                borderBottom: `1px solid ${theme.border}`,
              }}>
                <div>
                  <p style={{ margin: "0 0 2px", fontFamily: "'Georgia', serif", fontSize: "15px", color: theme.textPrimary }}>
                    {item.name}
                  </p>
                  <p style={{ margin: 0, fontSize: "13px", color: theme.textMuted }}>Qty: {item.quantity}</p>
                </div>
                <span style={{ fontFamily: "'Georgia', serif", fontSize: "15px", color: theme.textPrimary }}>
                  €{(Number(item.price) * item.quantity).toFixed(2)}
                </span>
              </div>
            ))}
            <div style={{ display: "flex", justifyContent: "space-between", padding: "20px 0" }}>
              <span style={{ fontSize: "11px", letterSpacing: "0.1em", textTransform: "uppercase", color: theme.textMuted }}>Total</span>
              <span style={{ fontFamily: "'Georgia', serif", fontSize: "24px", color: theme.textPrimary }}>€{total.toFixed(2)}</span>
            </div>
          </div>

          {/* Shipping address */}
          <p style={{ fontSize: "11px", letterSpacing: "0.15em", textTransform: "uppercase", color: theme.textAccent, marginBottom: "20px" }}>
            Shipping Address
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "16px", marginBottom: "40px" }}>
            <div>
              <label style={labelStyle}>Full Name</label>
              <input style={inputStyle} placeholder="Enter full name" value={shipping.fullName} onChange={e => setShipping(s => ({ ...s, fullName: e.target.value }))} />
            </div>
            <div>
              <label style={labelStyle}>Address Line 1</label>
              <input style={inputStyle} placeholder="Street address" value={shipping.addressLine1} onChange={e => setShipping(s => ({ ...s, addressLine1: e.target.value }))} />
            </div>
            <div>
              <label style={labelStyle}>Address Line 2 (optional)</label>
              <input style={inputStyle} placeholder="Apartment, suite, etc. (optional)" value={shipping.addressLine2} onChange={e => setShipping(s => ({ ...s, addressLine2: e.target.value }))} />
            </div>
            <div style={{ display: "flex", gap: "12px" }}>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>City</label>
                <input style={inputStyle} placeholder="City / Town" value={shipping.city} onChange={e => setShipping(s => ({ ...s, city: e.target.value }))} />
              </div>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>County</label>
                <input style={inputStyle} placeholder="County" value={shipping.county} onChange={e => setShipping(s => ({ ...s, county: e.target.value }))} />
              </div>
            </div>
            <div>
              <label style={labelStyle}>Eircode</label>
              <input style={inputStyle} placeholder="Eircode" value={shipping.eircode} onChange={e => setShipping(s => ({ ...s, eircode: e.target.value }))} />
            </div>
            {shippingError && (
              <p style={{ fontSize: "13px", color: theme.errorText, margin: 0, padding: "10px 12px", background: theme.errorBackground, borderRadius: "2px" }}>
                {shippingError}
              </p>
            )}
          </div>

          {/* Card details */}
          <p style={{ fontSize: "11px", letterSpacing: "0.15em", textTransform: "uppercase", color: theme.textAccent, marginBottom: "20px" }}>
            Card Details
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "16px", marginBottom: "32px" }}>
            <div>
              <label style={labelStyle}>Card Number</label>
              <input
                style={inputStyle}
                placeholder="Card number"
                value={card.number}
                onChange={e => setCard(c => ({ ...c, number: formatCardNumber(e.target.value) }))}
                maxLength={23}
              />
            </div>
            <div>
              <label style={labelStyle}>Name on Card</label>
              <input style={inputStyle} placeholder="Name as shown on card" value={card.name} onChange={e => setCard(c => ({ ...c, name: e.target.value }))} />
            </div>
            <div style={{ display: "flex", gap: "12px" }}>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>Expiry</label>
                <input
                  style={inputStyle}
                  placeholder="MM/YY"
                  value={card.expiry}
                  onChange={e => setCard(c => ({ ...c, expiry: formatExpiry(e.target.value) }))}
                  maxLength={5}
                />
              </div>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>CVV</label>
                <input
                  style={inputStyle}
                  placeholder="CVV"
                  value={card.cvv}
                  onChange={e => setCard(c => ({ ...c, cvv: e.target.value.replace(/\D/g, "").slice(0, 4) }))}
                  maxLength={4}
                  type="password"
                />
              </div>
            </div>
            {cardError && (
              <p style={{ fontSize: "13px", color: theme.errorText, margin: 0, padding: "10px 12px", background: theme.errorBackground, borderRadius: "2px" }}>
                {cardError}
              </p>
            )}
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

          {status === "FAILED" && (
            <p style={{
              fontSize: "13px",
              color: theme.errorText,
              marginBottom: "16px",
              padding: "12px 16px",
              background: theme.errorBackground,
              borderRadius: "2px",
            }}>
              Your card was declined. Please check your details and try again. Note: card numbers ending in 0000 are always declined.
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