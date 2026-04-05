import { useContext } from "react";
import { CartContext } from "../context/CartContext";

function ProductCard({ product }) {
  const { addToCart } = useContext(CartContext);

  const cleanProduct = {
    id: product.id,
    name: product.name,
    price: Number(product.price),
  };

  return (
    <div>
      <h3>{product.name}</h3>
      <p>€{product.price}</p>

      <button onClick={() => addToCart(cleanProduct)}>
        Add to Cart
      </button>
    </div>
  );
}

export default ProductCard;