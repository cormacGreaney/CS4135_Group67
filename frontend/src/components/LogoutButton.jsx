import { logout } from "../api/api.js";
import theme from "../styles/theme";

export default function LogoutButton() {
  return (
    <button
      onClick={logout}
      style={{
        background: "none",
        border: `1px solid ${theme.border}`,
        color: theme.textMuted,
        padding: "8px 16px",
        fontSize: "11px",
        letterSpacing: "0.1em",
        textTransform: "uppercase",
        cursor: "pointer",
        borderRadius: "2px",
        transition: "all 0.2s",
      }}
      onMouseEnter={e => {
        e.target.style.borderColor = theme.errorText;
        e.target.style.color = theme.errorText;
      }}
      onMouseLeave={e => {
        e.target.style.borderColor = theme.border;
        e.target.style.color = theme.textMuted;
      }}
    >
      Log Out
    </button>
  );
}