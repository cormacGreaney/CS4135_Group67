import { Link, useLocation } from "react-router-dom";
import { useContext } from "react";
import { CartContext } from "../context/CartContext.jsx";
import theme from "../styles/theme";

function Navbar() {
  const { cart } = useContext(CartContext);
  const location = useLocation();
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  const linkStyle = (path) => ({
    textDecoration: "none",
    fontSize: "13px",
    letterSpacing: "0.08em",
    textTransform: "uppercase",
    color: location.pathname === path ? theme.textPrimary : theme.textMuted,
    fontWeight: location.pathname === path ? "500" : "400",
    transition: "color 0.2s",
  });

  return (
    <nav style={{
      background: theme.backgroundWhite,
      borderBottom: `1px solid ${theme.border}`,
      padding: "0 40px",
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      height: "64px",
      position: "sticky",
      top: 0,
      zIndex: 100,
    }}>
      <Link to="/" style={{ textDecoration: "none" }}>
        <span style={{
          fontFamily: "'Georgia', serif",
          fontSize: "20px",
          fontWeight: "400",
          color: theme.textPrimary,
          letterSpacing: "0.02em",
        }}>
          Limerick Liquor
        </span>
      </Link>

      <div style={{ display: "flex", gap: "32px", alignItems: "center" }}>
        <Link to="/" style={linkStyle("/")}>Shop</Link>
        <Link to="/about" style={linkStyle("/about")}>About</Link>
        <Link to="/login" style={linkStyle("/login")}>Login</Link>
        <Link to="/register" style={linkStyle("/register")}>Register</Link>
        <Link to="/cart" style={{
          ...linkStyle("/cart"),
          background: theme.buttonPrimary,
          color: theme.buttonPrimaryText,
          padding: "8px 16px",
          borderRadius: "2px",
          fontSize: "12px",
        }}>
          Cart {cartCount > 0 ? `(${cartCount})` : ""}
        </Link>
      </div>
    </nav>
  );
}

export default Navbar;