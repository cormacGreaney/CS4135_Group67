import { useContext, useState } from "react";
import { CartContext } from "../context/CartContext";
import theme from "../styles/theme";

function ProductCard({ product }) {
  const { addToCart } = useContext(CartContext);
  const [added, setAdded] = useState(false);

  const categoryIcon = {
    wine: "🍷",
    beer: "🍺",
    spirits: "🥃",
    champagne: "🍾",
    cider: "🍏",
  };

  const icon = categoryIcon[product.category?.toLowerCase()] || "🚬";

  const cleanProduct = {
    id: product.id,
    name: product.name,
    price: Number(product.price),
  };

  function handleAdd() {
    addToCart(cleanProduct);
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
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
      }}>
        <span style={{ fontSize: "48px", opacity: 0.3 }}>{icon}</span>
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

      <div style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginTop: "auto",
        paddingTop: "12px",
        borderTop: `1px solid ${theme.border}`,
      }}>
        <span style={{
          fontFamily: "'Georgia', serif",
          fontSize: "18px",
          color: theme.textPrimary,
        }}>
          €{Number(product.price).toFixed(2)}
        </span>

        <button
          onClick={handleAdd}
          style={{
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
      </div>
    </div>
  );
}

export default ProductCard;