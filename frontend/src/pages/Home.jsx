import { useEffect, useState } from "react";
import ProductCard from "../components/ProductCard.jsx";
import { apiFetch } from "../api/api.js";

function Home() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    apiFetch("/api/products")
      .then(data => {
        console.log("First product:", data.content[0]);
        setProducts(data.content || []);
      })
      .catch(err => console.error(err));
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h2>Products</h2>
      {products.length === 0 ? (
        <p>Loading products...</p>
      ) : (
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))",
          gap: "15px"
        }}>
          {products.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </div>
  );
}

export default Home;