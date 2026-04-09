import theme from "../styles/theme";

function About() {
  return (
    <div>
      <div style={{
        background: theme.backgroundWarm,
        padding: "64px 40px",
        borderBottom: `1px solid ${theme.border}`,
        textAlign: "center",
      }}>
        <p style={{
          fontSize: "11px",
          letterSpacing: "0.15em",
          textTransform: "uppercase",
          color: theme.textAccent,
          marginBottom: "16px",
        }}>
          Our Story
        </p>
        <h1 style={{
          fontFamily: "'Georgia', serif",
          fontSize: "42px",
          fontWeight: "400",
          color: theme.textPrimary,
          margin: 0,
        }}>
          About Us
        </h1>
      </div>

      <div style={{
        maxWidth: "640px",
        margin: "64px auto",
        padding: "0 40px",
      }}>
        <p style={{
          fontFamily: "'Georgia', serif",
          fontSize: "20px",
          color: theme.textPrimary,
          lineHeight: "1.6",
          marginBottom: "24px",
          fontWeight: "400",
        }}>
          Limerick Liquor is a local off-licence bringing a carefully curated
          selection of spirits, wine, and beer to your doorstep.
        </p>
        <p style={{
          fontSize: "15px",
          color: theme.textMuted,
          lineHeight: "1.8",
          marginBottom: "24px",
        }}>
          We believe great drinks shouldn't be hard to find. Whether you're after
          a reliable bottle of wine for dinner or something special for a gift,
          we've done the selecting so you don't have to.
        </p>
        <p style={{
          fontSize: "15px",
          color: theme.textMuted,
          lineHeight: "1.8",
        }}>
          Based in Limerick, serving across Ireland.
        </p>

        <div style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr 1fr",
          gap: "1px",
          background: theme.border,
          border: `1px solid ${theme.border}`,
          marginTop: "48px",
        }}>
          {[
            { number: "100%", label: "Irish owned" },
            { number: "15+", label: "Products" },
            { number: "Next day", label: "Delivery" },
          ].map(stat => (
            <div key={stat.label} style={{
              background: theme.backgroundWhite,
              padding: "32px 24px",
              textAlign: "center",
            }}>
              <p style={{
                fontFamily: "'Georgia', serif",
                fontSize: "28px",
                color: theme.textPrimary,
                margin: "0 0 4px",
              }}>{stat.number}</p>
              <p style={{
                fontSize: "11px",
                letterSpacing: "0.1em",
                textTransform: "uppercase",
                color: theme.textAccent,
                margin: 0,
              }}>{stat.label}</p>
            </div>
          ))}
        </div>

        {/* Contact */}
        <div style={{
          marginTop: "48px",
          paddingTop: "40px",
          borderTop: `1px solid ${theme.border}`,
        }}>
          <p style={{
            fontSize: "11px",
            letterSpacing: "0.15em",
            textTransform: "uppercase",
            color: theme.textAccent,
            marginBottom: "24px",
          }}>
            Get in Touch
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>

            {/* Email */}
            <div style={{ display: "flex", gap: "16px", alignItems: "flex-start" }}>
              <span style={{
                fontSize: "13px",
                color: theme.textMuted,
                minWidth: "60px",
                letterSpacing: "0.05em"
              }}>
                Email
              </span>
              <a
                href="mailto:limerickliquor@gmail.com"
                style={{
                  fontSize: "15px",
                  color: theme.textPrimary,
                  textDecoration: "none",
                  borderBottom: `1px solid ${theme.border}`
                }}
              >
                limerickliquor@gmail.com
              </a>
            </div>

            {/* Phone */}
            <div style={{ display: "flex", gap: "16px", alignItems: "flex-start" }}>
              <span style={{
                fontSize: "13px",
                color: theme.textMuted,
                minWidth: "60px",
                letterSpacing: "0.05em"
              }}>
                Phone
              </span>
              <a
                href="tel:+35361234567"
                style={{
                  fontSize: "15px",
                  color: theme.textPrimary,
                  textDecoration: "none",
                  borderBottom: `1px solid ${theme.border}`
                }}
              >
                +353 61 234 567
              </a>
            </div>

            {/* Address */}
            <div style={{ display: "flex", gap: "16px", alignItems: "flex-start" }}>
              <span style={{
                fontSize: "13px",
                color: theme.textMuted,
                minWidth: "60px",
                letterSpacing: "0.05em"
              }}>
                Address
              </span>
              <p style={{
                fontSize: "15px",
                color: theme.textPrimary,
                margin: 0,
                lineHeight: "1.6"
              }}>
                O'Connell Street<br />
                Limerick City<br />
                V96 X2K1
              </p>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}

export default About;