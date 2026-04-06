import { useContext, useState } from "react";
import { CartContext } from "../context/CartContext";
import theme from "../styles/theme";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function ProductCard({ product }) {
  const { cart, addToCart } = useContext(CartContext);
  const [added, setAdded] = useState(false);
  const [imgError, setImgError] = useState(false);
  const [quantity, setQuantity] = useState(1);

  const categoryIcon = {
    wine: "🍷",
    beer: "🍺",
    spirits: "🥃",
    champagne: "🍾",
    cider: "🍏",
  };

  const icon = categoryIcon[product.category?.toLowerCase()] || "🍺";

  const stock = product.stockQuantity ?? Infinity;
  const inCart = cart.find(p => p.id === product.id)?.quantity ?? 0;
  const remaining = stock - inCart;

  const cleanProduct = {
    id: product.id,
    name: product.name,
    price: Number(product.price),
    stockQuantity: product.stockQuantity,
};

  function handleAdd() {
    const toAdd = Math.min(quantity, remaining);
    for (let i = 0; i < toAdd; i++) {
      addToCart(cleanProduct);
    }
    setAdded(true);
    setQuantity(1);
    setTimeout(() => setAdded(false), 1500);
  }

  function handleQuantityInput(val) {
    const parsed = parseInt(val);
    if (!isNaN(parsed) && parsed >= 1) setQuantity(Math.min(parsed, remaining));
    else if (val === "") setQuantity("");
  }

  function handleQuantityBlur() {
    if (quantity === "" || quantity < 1) setQuantity(1);
  }

  return (
    <div style={{
      background: theme.backgroundWhite,
      border: `1px solid ${theme.border}`,
      padding: "24px",
      display: "flex",
      flexDirection: "column",
      gap: "12px",
    }}>
      <div style={{
        background: theme.backgroundWarm,
        height: "180px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        marginBottom: "4px",
        overflow: "hidden",
      }}>
        {!imgError ? (
          <img
            src={`${API_BASE}/api/products/${product.id}/image`}
            alt={product.name}
            onError={() => setImgError(true)}
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
          />
        ) : (
          <span style={{ fontSize: "48px", opacity: 0.3 }}>{icon}</span>
        )}
      </div>

      {product.category && (
        <span style={{
          fontSize: "10px",
          letterSpacing: "0.12em",
          textTransform: "uppercase",
          color: theme.textAccent,
          fontWeight: "500",
        }}>
          {product.category}
        </span>
      )}

      <h3 style={{
        margin: 0,
        fontSize: "16px",
        fontWeight: "400",
        fontFamily: "'Georgia', serif",
        color: theme.textPrimary,
        lineHeight: "1.3",
      }}>
        {product.name}
      </h3>

      {product.description && (
        <p style={{
          margin: 0,
          fontSize: "13px",
          color: theme.textMuted,
          lineHeight: "1.5",
        }}>
          {product.description}
        </p>
      )}

      <div style={{ marginTop: "auto", paddingTop: "12px", borderTop: `1px solid ${theme.border}` }}>
        <div style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "10px",
        }}>
          <span style={{
            fontFamily: "'Georgia', serif",
            fontSize: "18px",
            color: theme.textPrimary,
          }}>
            €{Number(product.price).toFixed(2)}
          </span>

          <div style={{
            display: "flex",
            alignItems: "center",
            border: `1px solid ${theme.border}`,
            borderRadius: "2px",
            opacity: remaining <= 0 ? 0.4 : 1,
          }}>
            <button
              onClick={() => setQuantity(q => Math.max(1, q - 1))}
              disabled={remaining <= 0}
              style={{
                background: "none", border: "none", padding: "6px 10px",
                cursor: remaining <= 0 ? "not-allowed" : "pointer",
                color: theme.textMuted, fontSize: "16px", lineHeight: 1,
              }}
            >
              −
            </button>
            <input
              type="number"
              value={quantity}
              onChange={e => handleQuantityInput(e.target.value)}
              onBlur={handleQuantityBlur}
              disabled={remaining <= 0}
              min="1"
              max={remaining}
              style={{
                width: "36px", border: "none",
                borderLeft: `1px solid ${theme.border}`,
                borderRight: `1px solid ${theme.border}`,
                textAlign: "center", fontSize: "13px",
                color: theme.textPrimary, padding: "6px 0",
                outline: "none", MozAppearance: "textfield",
              }}
            />
            <button
              onClick={() => setQuantity(q => Math.min(q + 1, remaining))}
              disabled={remaining <= 0}
              style={{
                background: "none", border: "none", padding: "6px 10px",
                cursor: remaining <= 0 ? "not-allowed" : "pointer",
                color: theme.textMuted, fontSize: "16px", lineHeight: 1,
              }}
            >
              +
            </button>
          </div>
        </div>

        {remaining <= 0 ? (
          <div style={{
            width: "100%",
            background: theme.backgroundWarm,
            color: theme.textMuted,
            border: `1px solid ${theme.border}`,
            padding: "8px 16px",
            fontSize: "11px",
            letterSpacing: "0.1em",
            textTransform: "uppercase",
            textAlign: "center",
            borderRadius: "2px",
            boxSizing: "border-box",
          }}>
            Out of Stock
          </div>
        ) : (
          <button
            onClick={handleAdd}
            style={{
              width: "100%",
              background: added ? theme.success : theme.buttonPrimary,
              color: theme.buttonPrimaryText,
              border: "none",
              padding: "8px 16px",
              fontSize: "11px",
              letterSpacing: "0.1em",
              textTransform: "uppercase",
              cursor: "pointer",
              borderRadius: "2px",
              transition: "background 0.3s",
            }}
          >
            {added ? "Added" : "Add to Cart"}
          </button>
        )}

        {remaining > 0 && remaining <= 5 && (
          <p style={{
            margin: "8px 0 0",
            fontSize: "11px",
            color: theme.textAccent,
            textAlign: "center",
          }}>
            Only {remaining} left
          </p>
        )}
      </div>
    </div>
  );
}

export default ProductCard;