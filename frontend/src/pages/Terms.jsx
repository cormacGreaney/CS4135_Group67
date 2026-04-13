import theme from "../styles/theme";

function Terms() {
  return (
    <div
      style={{
        backgroundColor: theme.backgroundWarm,
        minHeight: "calc(100vh - 64px)",
        padding: "30px 20px",
        boxSizing: "border-box",
      }}
    >
      <div
        style={{
          maxWidth: "800px",
          margin: "0 auto",
          backgroundColor: theme.backgroundWhite,
          padding: "30px",
          borderRadius: "12px",
          border: `1px solid ${theme.border}`,
          boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
        }}
      >
        <h1
          style={{
            fontFamily: "'Georgia', serif",
            color: theme.textPrimary,
            borderBottom: `2px solid ${theme.textAccent}`,
            paddingBottom: "10px",
          }}
        >
          Terms & Conditions
        </h1>

        <p style={{ fontFamily: "'Arial', sans-serif", marginTop: "20px" }}>
          Welcome to our e-commerce off-license platform.
        </p>

        <p style={{ fontFamily: "'Arial', sans-serif" }}>
          By creating an account, you agree to the following:
        </p>

        <ul style={{ fontFamily: "'Arial', sans-serif", lineHeight: "1.6" }}>
          <li>You must be at least 18 years old to use this service.</li>
          <li>All purchases are subject to availability.</li>
          <li>We reserve the right to refuse service at any time.</li>
        </ul>

        <p style={{ fontFamily: "'Arial', sans-serif", marginTop: "20px" }}>
          These terms may change at any time without notice.
        </p>
      </div>
    </div>
  );
}

export default Terms;