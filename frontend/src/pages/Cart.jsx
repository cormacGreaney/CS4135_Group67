import { useContext } from "react";
import { CartContext } from "../context/CartContext.jsx";
import { Link } from "react-router-dom";

function Cart() {
  const { cart } = useContext(CartContext);

  const total = cart.reduce((sum, item) => sum + item.price, 0);

  return (
    <div style={{ padding: "20px" }}>
      <h2>Your Cart</h2>
      {cart.length === 0 ? <p>Cart is empty</p> : (
        <>
          <ul>
            {cart.map((item, i) => (
              <li key={i}>{item.name} - €{item.price}</li>
            ))}
          </ul>
          <p>Total: €{total.toFixed(2)}</p>
          <Link to="/checkout"><button>Checkout</button></Link>
        </>
      )}
    </div>
  );
}

export default Cart;