import { useEffect, useState } from 'react'
import { authFetch } from './api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function Home({ user, onLogout }) {

  const [foods, setFoods] = useState([])
  const [search, setSearch] = useState("")
  const [cart, setCart] = useState([]); // Array of { id, name, price, category, imageurl, qty }
  const [paymentMethod, setPaymentMethod] = useState("");
  const [showPayment, setShowPayment] = useState(false);
  const [showCart, setShowCart] = useState(false);
  const [myOrders, setMyOrders] = useState([]);
  const [showOrders, setShowOrders] = useState(false);
  const [isSubmittingOrder, setIsSubmittingOrder] = useState(false);
  const [isAddingFood, setIsAddingFood] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [orderSuccessMsg, setOrderSuccessMsg] = useState("");
  const [newFood,setNewFood]=useState({
    name:"",
    price:"",
    category:"",
    imageurl:""
  });

  const fetchAllFoods = () => {
    setIsSearching(true);
    fetch(`${API_BASE_URL}/foods`)
      .then(async (res) => {
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || "Failed to load food menu");
        }
        return res.json();
      })
      .then((data) => {
        setFoods(Array.isArray(data) ? data : []);
        setIsSearching(false);
      })
      .catch((err) => {
        console.error("Error fetching foods:", err);
        setIsSearching(false);
      });
  };

  const fetchMyOrders = () => {
    authFetch(`${API_BASE_URL}/orders/my-orders`)
      .then(async (res) => {
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || "Failed to fetch order history");
        }
        return res.json();
      })
      .then((data) => setMyOrders(Array.isArray(data) ? data : []))
      .catch((err) => {
        console.error("Error fetching orders:", err);
        alert(err.message || "Failed to fetch orders");
      });
  };

  useEffect(()=>{
    fetchAllFoods();
  },[]);

  const handleChange=(e)=>{
    setNewFood({...newFood,[e.target.name]:e.target.value});
  };

  const addFood=()=>{
    if (!newFood.name.trim() || !newFood.price || !newFood.category.trim()) {
      alert("Please fill in Food Name, Price, and Category.");
      return;
    }
    const parsedPrice = parseFloat(newFood.price);
    if (isNaN(parsedPrice) || parsedPrice <= 0) {
      alert("Price must be a valid number greater than zero.");
      return;
    }

    setIsAddingFood(true);
    authFetch(`${API_BASE_URL}/foods`,{
      method:"post",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({
        ...newFood,
        price: parsedPrice,
      })
    })
    .then(async (res) => {
      const data = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(data.message || (data.errors ? Object.values(data.errors).join(", ") : "Failed to add food"));
      }
      return data;
    })
    .then(data=>{
      setIsAddingFood(false);
      setFoods([...foods,data]);
      setNewFood({
        name:"",
        price:"",
        category:"",
        imageurl:""
      });
      alert("Food added successfully!");
    })
    .catch(err => {
      setIsAddingFood(false);
      console.error("Error adding food:", err);
      alert(err.message || "Error adding food");
    });
  };

  const deleteFood = (id) => {
    authFetch(`${API_BASE_URL}/foods/${id}`, {
      method: "DELETE"
    }).then((res) => {
      if (!res.ok) throw new Error("Failed to delete food");
      setFoods(foods.filter(food => food.id !== id));
    })
    .catch(err => {
      console.error("Error deleting food:", err);
      alert(err.message || "Error deleting food");
    });
  };

  // Add food to cart — group by id, incrementing qty
  const placeOrder = (food) => {
    setCart(prev => {
      const existing = prev.find(item => item.id === food.id);
      if (existing) {
        return prev.map(item =>
          item.id === food.id ? { ...item, qty: item.qty + 1 } : item
        );
      }
      return [...prev, { ...food, qty: 1 }];
    });
  };

  // Increase quantity of a cart item
  const increaseQty = (foodId) => {
    setCart(prev => prev.map(item =>
      item.id === foodId ? { ...item, qty: item.qty + 1 } : item
    ));
  };

  // Decrease quantity — remove item if it reaches 0
  const decreaseQty = (foodId) => {
    setCart(prev => {
      const updated = prev.map(item =>
        item.id === foodId ? { ...item, qty: item.qty - 1 } : item
      );
      return updated.filter(item => item.qty > 0);
    });
  };

  // Remove an item from cart entirely
  const removeFromCart = (foodId) => {
    setCart(prev => prev.filter(item => item.id !== foodId));
  };

  // Compute cart total using integer math to avoid floating-point drift
  const cartTotal = cart.reduce((sum, item) => {
    return Math.round(sum * 100 + item.price * item.qty * 100) / 100;
  }, 0);

  const handlePayment = (method) => {
    if (cart.length === 0) return;
    setPaymentMethod(method);
    setIsSubmittingOrder(true);

    // Build payload from consolidated cart (each item already has qty field)
    const itemsPayload = cart.map(item => ({
      foodId: item.id,
      quantity: item.qty,
    }));

    authFetch(`${API_BASE_URL}/orders`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        items: itemsPayload,
        paymentMethod: method
      })
    })
      .then(async res => {
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          throw new Error(data.message || "Order placement failed");
        }
        return data;
      })
      .then(orderData => {
        setIsSubmittingOrder(false);
        setOrderSuccessMsg(`🎉 Order #${orderData.id} Confirmed! Total: ₹${orderData.totalAmount} (via ${method})`);
        setCart([]);
        setShowPayment(false);
        setShowCart(false);
        fetchMyOrders();
      })
      .catch(err => {
        setIsSubmittingOrder(false);
        console.error("Order error:", err);
        alert(err.message || "Failed to place order");
      });
  };

  return (
    <div className='container'>

      <div className="navbar">
        <h1 className="logo">🍔 SnackSpot</h1>
        <div>
          <span>Welcome {typeof user === 'object' ? user.username : user}</span>
          <button onClick={() => { setShowOrders(!showOrders); if (!showOrders) fetchMyOrders(); }}>
            📦 My Orders {myOrders.length > 0 && `(${myOrders.length})`}
          </button>
          <button onClick={() => setShowCart(!showCart)}>
            🛒 Cart ({cart.reduce((sum, item) => sum + item.qty, 0)})
          </button>
          <button onClick={onLogout}>Logout</button>
        </div>
      </div>

      {orderSuccessMsg && (
        <div style={{ backgroundColor: "#d4edda", color: "#155724", padding: "12px", borderRadius: "6px", margin: "16px 0", border: "1px solid #c3e6cb", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span>{orderSuccessMsg}</span>
          <button onClick={() => setOrderSuccessMsg("")} style={{ background: "none", border: "none", fontSize: "16px", cursor: "pointer", color: "#155724" }}>✖</button>
        </div>
      )}

      {/* MY ORDERS SECTION */}
      {showOrders && (
        <div className="cart" style={{ marginBottom: "20px" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <h2>📦 My Order History</h2>
            <button onClick={() => setShowOrders(false)}>Close</button>
          </div>
          {myOrders.length === 0 ? (
            <p>You haven't placed any orders yet.</p>
          ) : (
            myOrders.map(order => (
              <div key={order.id} style={{ borderBottom: "1px solid #eee", padding: "12px 0" }}>
                <div style={{ display: "flex", justifyContent: "space-between", fontWeight: "bold" }}>
                  <span>Order #{order.id}</span>
                  <span style={{ color: order.orderStatus === 'DELIVERED' ? 'green' : order.orderStatus === 'CANCELLED' ? 'red' : '#ff9800' }}>
                    {order.orderStatus}
                  </span>
                </div>
                <div style={{ fontSize: "12px", color: "#666", margin: "4px 0" }}>
                  Payment: {order.paymentMethod} | Date: {order.createdAt ? new Date(order.createdAt).toLocaleString() : ""}
                </div>
                <ul style={{ margin: "6px 0", paddingLeft: "20px" }}>
                  {order.items && order.items.map(item => (
                    <li key={item.id} style={{ fontSize: "14px" }}>
                      {item.foodName} x {item.quantity} - ₹{item.price} each (Subtotal: ₹{item.subtotal})
                    </li>
                  ))}
                </ul>
                <div style={{ fontWeight: "bold", textAlign: "right" }}>
                  Total: ₹{order.totalAmount}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Only show food management options if user has admin privileges */}
      {user?.role === "ADMIN" && (
        <>
          <h2>Add To SnackSpot</h2>
          <div className="form">
            <input name="name" placeholder='Food name' value={newFood.name} onChange={handleChange}/>
            <input name="price" placeholder='price' value={newFood.price} onChange={handleChange}/>
            <input name="category" placeholder='Category' value={newFood.category} onChange={handleChange}/>
            <input name="imageurl" placeholder='Image URL' value={newFood.imageurl} onChange={handleChange}/>
            <button onClick={addFood} disabled={isAddingFood}>
              {isAddingFood ? "Adding..." : "Add Food"}
            </button>
          </div>
        </>
      )}

      <div className="searchBox">
        <input 
          type="text"
          placeholder="Search food..."
          value={search}
          onChange={(e)=>{
            const value = e.target.value;
            setSearch(value);

            if(value.trim() === ""){
              fetchAllFoods();   
            } else {
              setIsSearching(true);
              fetch(`${API_BASE_URL}/foods/search?name=${encodeURIComponent(value.trim())}`)
                .then(res => res.json())
                .then(data => {
                  setFoods(Array.isArray(data) ? data : []);
                  setIsSearching(false);
                })
                .catch(err => {
                  console.error("Error searching food:", err);
                  setIsSearching(false);
                });
            }
          }}
        />
      </div>

      <div className='grid'>
        {foods.map(food =>(
          <div className='card' key={food.id}>
            <img src={food.imageurl} alt={food.name}/>
            <h3>{food.name}</h3>
            <p>₹{food.price}</p>
            <span>{food.category}</span>

            <div className='buttons'>
              <button className='order' onClick={() => placeOrder(food)}>
                Order
              </button>

              {user?.role === "ADMIN" && (
                <button className='delete' onClick={()=>deleteFood(food.id)}>
                  Delete              
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
      {showCart && (
  <div className="cart">
    <h2>🛒 Your Cart</h2>

    {cart.length === 0 && <p>No items in cart</p>}

    {cart.map((item) => (
      <div key={item.id} className="cart-item" style={{ display: "flex", alignItems: "center", gap: "8px" }}>
        <span style={{ flex: 1 }}>{item.name}</span>
        <span style={{ minWidth: "56px", textAlign: "right" }}>₹{(item.price * item.qty).toFixed(2)}</span>
        <button
          onClick={() => decreaseQty(item.id)}
          style={{ padding: "2px 8px", fontSize: "16px", lineHeight: 1, background: "#E8D5B7", border: "none", borderRadius: "4px", cursor: "pointer", color: "#3B2517", fontWeight: 700 }}
        >−</button>
        <span style={{ minWidth: "20px", textAlign: "center", fontWeight: 700 }}>{item.qty}</span>
        <button
          onClick={() => increaseQty(item.id)}
          style={{ padding: "2px 8px", fontSize: "16px", lineHeight: 1, background: "#E8D5B7", border: "none", borderRadius: "4px", cursor: "pointer", color: "#3B2517", fontWeight: 700 }}
        >+</button>
        <button
          onClick={() => removeFromCart(item.id)}
          style={{ padding: "2px 8px", fontSize: "13px", background: "#A44A3F", border: "none", borderRadius: "4px", cursor: "pointer", color: "#FFFDF9", fontWeight: 600 }}
        >✕</button>
      </div>
    ))}

    <h3>
      Total: ₹{cartTotal.toFixed(2)}
    </h3>

    {cart.length > 0 && !showPayment && (
      <button
        className="pay-btn"
        onClick={() => setShowPayment(true)}
      >
        Proceed To Pay
      </button>
    )}

    {showPayment && (
      <div className="payment-section">
        <h4>Select Payment Method</h4>

        <button disabled={isSubmittingOrder} onClick={() => handlePayment("UPI")}>
          {isSubmittingOrder ? "Placing Order..." : "Pay by UPI"}
        </button>

        <button disabled={isSubmittingOrder} onClick={() => handlePayment("COD")}>
          {isSubmittingOrder ? "Placing Order..." : "Cash on Delivery"}
        </button>
      </div>
    )}
  </div>
)}

    </div>
  );
}

export default Home;
