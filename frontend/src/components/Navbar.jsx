import { Link, useLocation, useNavigate } from "react-router-dom";
import { useContext } from "react";
import { CartContext } from "../context/CartContext.jsx";
import { AuthContext } from "../context/AuthContext.jsx";
import theme from "../styles/theme";

function Navbar() {
  const { cart } = useContext(CartContext);
  const { user } = useContext(AuthContext);
  const location = useLocation();
  const navigate = useNavigate();
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  const accountPath = user?.role === "ADMINISTRATOR" ? "/admin" : "/dashboard";
  const isAccountActive = location.pathname === "/dashboard" || location.pathname === "/admin";
  const isAuthActive = location.pathname === "/login" || location.pathname === "/register";

  const linkStyle = (path, isActiveOverride = null) => ({
    textDecoration: "none",
    fontSize: "13px",
    letterSpacing: "0.08em",
    textTransform: "uppercase",
    color: (isActiveOverride ?? location.pathname === path) ? theme.textPrimary : theme.textMuted,
    fontWeight: (isActiveOverride ?? location.pathname === path) ? "500" : "400",
    transition: "color 0.2s",
  });

  function goToShop() {
    navigate("/", { state: { clearFilters: true } });
  }

  return (
    <nav style={{
      background: theme.backgroundWhite,
      borderBottom: `1px solid ${theme.border}`,
      boxSizing: "border-box",
      padding: "0 40px",
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      height: "64px",
      position: "sticky",
      top: 0,
      zIndex: 100,
    }}>
      <span onClick={goToShop} style={{ textDecoration: "none", cursor: "pointer" }}>
        <span style={{
          fontFamily: "'Georgia', serif",
          fontSize: "20px",
          fontWeight: "400",
          color: theme.textPrimary,
          letterSpacing: "0.02em",
        }}>
          Limerick Liquor
        </span>
      </span>

      <div style={{ display: "flex", gap: "32px", alignItems: "center" }}>
        <span onClick={goToShop} style={{ ...linkStyle("/"), cursor: "pointer" }}>Shop</span>
        <Link to="/about" style={linkStyle("/about")}>About</Link>
        {user ? (
          <Link to={accountPath} style={linkStyle(accountPath, isAccountActive)}>Account</Link>
        ) : (
          <Link to="/login" style={linkStyle("/login", isAuthActive)}>Login / Register</Link>
        )}
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