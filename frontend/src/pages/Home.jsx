import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import ProductCard from "../components/ProductCard.jsx";
import { apiFetch } from "../api/api.js";
import theme from "../styles/theme";

function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [ageVerified, setAgeVerified] = useState(
    () => sessionStorage.getItem("ageVerified") === "true"
  );
  const navigate = useNavigate();
  const location = useLocation();

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

  useEffect(() => {
    if (location.state?.clearFilters) {
      clearFilters();
      navigate("/", { replace: true, state: {} });
    }
  }, [location.state, navigate]);

  function handleAgeConfirm() {
    sessionStorage.setItem("ageVerified", "true");
    setAgeVerified(true);
  }

  function handleAgeDeny() {
    window.location.href = "https://www.google.com";
  }

  function clearFilters() {
    setSearch("");
    setCategory("");
    setMaxPrice("");
  }

  const hasFilters = search !== "" || category !== "" || maxPrice !== "";

  const aliasMap = {
    "holy water": "guinness",
    "the black stuff": "guinness",
  };

  const searchTerm = aliasMap[search.toLowerCase()] ?? search.toLowerCase();
  const categories = [...new Set(products.map(p => p.category).filter(Boolean))];

  const filtered = products.filter(p => {
    const matchesName = p.name.toLowerCase().includes(searchTerm);
    const matchesCategory = category === "" || p.category === category;
    const matchesPrice = maxPrice === "" || Number(p.price) <= Number(maxPrice);
    return matchesName && matchesCategory && matchesPrice;
  });

  return (
    <div>
      {!ageVerified && (
        <div style={{
          position: "fixed",
          inset: 0,
          background: "rgba(0,0,0,0.7)",
          zIndex: 1000,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}>
          <div style={{
            background: theme.backgroundWhite,
            padding: "48px 40px",
            maxWidth: "400px",
            width: "90%",
            textAlign: "center",
            border: `1px solid ${theme.border}`,
          }}>
            <p style={{
              fontSize: "11px",
              letterSpacing: "0.15em",
              textTransform: "uppercase",
              color: theme.textAccent,
              marginBottom: "16px",
            }}>
              Age Verification
            </p>
            <h2 style={{
              fontFamily: "'Georgia', serif",
              fontSize: "26px",
              fontWeight: "400",
              color: theme.textPrimary,
              margin: "0 0 16px",
            }}>
              Are you over 18?
            </h2>
            <p style={{
              color: theme.textMuted,
              fontSize: "14px",
              lineHeight: "1.6",
              margin: "0 0 32px",
            }}>
              You must be 18 or older to enter this site. Please confirm your age.
            </p>
            <div style={{ display: "flex", gap: "12px", justifyContent: "center" }}>
              <button
                onClick={handleAgeConfirm}
                style={{
                  background: theme.buttonPrimary,
                  color: theme.buttonPrimaryText,
                  border: "none",
                  padding: "12px 28px",
                  fontSize: "11px",
                  letterSpacing: "0.1em",
                  textTransform: "uppercase",
                  cursor: "pointer",
                  borderRadius: "2px",
                }}
              >
                Yes, I am 18+
              </button>
              <button
                onClick={handleAgeDeny}
                style={{
                  background: "none",
                  color: theme.textMuted,
                  border: `1px solid ${theme.border}`,
                  padding: "12px 28px",
                  fontSize: "11px",
                  letterSpacing: "0.1em",
                  textTransform: "uppercase",
                  cursor: "pointer",
                  borderRadius: "2px",
                }}
              >
                No
              </button>
            </div>
          </div>
        </div>
      )}

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

        <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap", alignItems: "center" }}>
          <input
            type="text"
            placeholder="Search products..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{
              width: "100%",
              maxWidth: "200px",
              padding: "12px 16px",
              fontSize: "14px",
              border: `1px solid ${theme.border}`,
              borderRadius: "2px",
              background: theme.backgroundWhite,
              outline: "none",
              color: theme.textPrimary,
            }}
          />
          <select
            value={category}
            onChange={e => setCategory(e.target.value)}
            style={{
              padding: "12px 16px",
              fontSize: "14px",
              border: `1px solid ${theme.border}`,
              borderRadius: "2px",
              background: theme.backgroundWhite,
              outline: "none",
              color: category === "" ? theme.textMuted : theme.textPrimary,
              cursor: "pointer",
            }}
          >
            <option value="">All categories</option>
            {categories.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
          <input
            type="number"
            placeholder="Max price (€)"
            value={maxPrice}
            onChange={e => setMaxPrice(e.target.value)}
            min="0"
            style={{
              width: "130px",
              padding: "12px 16px",
              fontSize: "14px",
              border: `1px solid ${theme.border}`,
              borderRadius: "2px",
              background: theme.backgroundWhite,
              outline: "none",
              color: theme.textPrimary,
            }}
          />
          {hasFilters && (
            <button
              onClick={clearFilters}
              style={{
                background: "none",
                border: `1px solid ${theme.border}`,
                color: theme.textMuted,
                padding: "12px 16px",
                fontSize: "11px",
                letterSpacing: "0.1em",
                textTransform: "uppercase",
                cursor: "pointer",
                borderRadius: "2px",
                whiteSpace: "nowrap",
              }}
            >
              Clear Filters
            </button>
          )}
        </div>
      </div>

      <div style={{ padding: "48px 40px" }}>
        {loading ? (
          <p style={{ color: theme.textMuted, textAlign: "center" }}>Loading products...</p>
        ) : filtered.length === 0 ? (
          <div style={{ textAlign: "center" }}>
            <p style={{ color: theme.textMuted, marginBottom: "16px" }}>No products found.</p>
            <button
              onClick={clearFilters}
              style={{
                background: "none",
                border: `1px solid ${theme.border}`,
                color: theme.textMuted,
                padding: "10px 20px",
                fontSize: "11px",
                letterSpacing: "0.1em",
                textTransform: "uppercase",
                cursor: "pointer",
                borderRadius: "2px",
              }}
            >
              Clear Filters
            </button>
          </div>
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