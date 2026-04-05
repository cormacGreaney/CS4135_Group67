import { useContext } from "react";
import { CartContext } from "../context/CartContext.jsx";

function Checkout() {
  const { cart } = useContext(CartContext);

  function handlePayment() {
    alert("Payment API integration goes here!");
  }

  const total = cart.reduce((sum, item) => sum + item.price, 0);

  return (
    <div style={{ padding: "20px" }}>
      <h2>Checkout</h2>
      <ul>
        {cart.map((item, i) => <li key={i}>{item.name} - €{item.price}</li>)}
      </ul>
      <p>Total: €{total.toFixed(2)}</p>
      <button onClick={handlePayment}>Pay Now</button>
    </div>
  );
}

export default Checkout;