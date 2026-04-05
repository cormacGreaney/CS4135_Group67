import { createContext, useState } from "react";

export const CartContext = createContext();

export function CartProvider({ children }) {
  const [cart, setCart] = useState([]);

  function addToCart(product) {
  setCart(prev => {
    const existing = prev.find(p => p.id === product.id);

    if (existing) {
      return prev.map(p =>
        p.id === product.id
          ? { ...p, quantity: (Number(p.quantity) || 0) + 1 }
          : p
      );
    }

    return [...prev, { ...product, price: Number(product.price), quantity: 1 }];
  });
}

  function removeFromCart(id) {
    setCart(prev => prev.filter(p => p.id !== id));
  }

  function clearCart() {
    setCart([]);
  }

  return (
    <CartContext.Provider value={{ cart, addToCart, removeFromCart, clearCart }}>
      {children}
    </CartContext.Provider>
  );
}