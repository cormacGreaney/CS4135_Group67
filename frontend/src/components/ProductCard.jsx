import { useContext } from "react";
import { CartContext } from "../context/CartContext.jsx";

function ProductCard({ product }) {
  const { addToCart } = useContext(CartContext);

  return (
    <div style={{
      border: "1px solid #ccc",
      padding: "10px",
      borderRadius: "5px"
    }}>
      <h3>{product.name}</h3>
      <p>Price: €{product.price}</p>
      <button onClick={() => addToCart(product)}>Add to Cart</button>
    </div>
  );
}

export default ProductCard;