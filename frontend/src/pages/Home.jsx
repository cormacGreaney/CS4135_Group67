import { useEffect, useState } from "react";
import ProductCard from "../components/ProductCard.jsx";
import { apiFetch } from "../api/api.js";
import theme from "../styles/theme";

function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  useEffect(() => {
    apiFetch("/api/products")
      .then(data => {
        setProducts(data.content || []);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  const aliasMap = {
    "holy water": "guinness",
    "the black stuff": "guinness",
  };

  const searchTerm = aliasMap[search.toLowerCase()] ?? search.toLowerCase();

  const filtered = products.filter(p =>
    p.name.toLowerCase().includes(searchTerm)
  );

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
          Limerick's Finest
        </p>
        <h1 style={{
          fontFamily: "'Georgia', serif",
          fontSize: "42px",
          fontWeight: "400",
          color: theme.textPrimary,
          margin: "0 0 16px",
          lineHeight: "1.2",
        }}>
          Curated Spirits & Wine
        </h1>
        <p style={{
          color: theme.textMuted,
          fontSize: "15px",
          maxWidth: "400px",
          margin: "0 auto 32px",
          lineHeight: "1.6",
        }}>
          A carefully selected collection of premium beverages, delivered to your door.
        </p>

        <input
          type="text"
          placeholder="Search products..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{
            width: "100%",
            maxWidth: "360px",
            padding: "12px 16px",
            fontSize: "14px",
            border: `1px solid ${theme.border}`,
            borderRadius: "2px",
            background: theme.backgroundWhite,
            outline: "none",
            color: theme.textPrimary,
          }}
        />
      </div>

      <div style={{ padding: "48px 40px" }}>
        {loading ? (
          <p style={{ color: theme.textMuted, textAlign: "center" }}>Loading products...</p>
        ) : filtered.length === 0 ? (
          <p style={{ color: theme.textMuted, textAlign: "center" }}>No products found.</p>
        ) : (
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))",
            gap: "1px",
            background: theme.border,
            border: `1px solid ${theme.border}`,
          }}>
            {filtered.map(p => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Home;