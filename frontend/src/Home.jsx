import { useEffect, useState } from 'react'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function Home({ user, onLogout }) {

  const [foods, setFoods] = useState([])
  const [search, setSearch] = useState("")
  const [cart, setCart] = useState([]);
  const [paymentMethod, setPaymentMethod] = useState("");
const [showPayment, setShowPayment] = useState(false);
const [showCart, setShowCart] = useState(false);
  const [newFood,setNewFood]=useState({
    name:"",
    price:"",
    category:"",
    imageurl:""
  });

  const fetchAllFoods = () => {
    fetch(`${API_BASE_URL}/foods`)
    .then(res=>res.json())
    .then(data=>setFoods(data))
    .catch(err => console.error("Error fetching foods:", err));
  }

  useEffect(()=>{
    fetchAllFoods();
  },[]);

  const handleChange=(e)=>{
    setNewFood({...newFood,[e.target.name]:e.target.value});
  };

  const addFood=()=>{
    fetch(`${API_BASE_URL}/foods`,{
      method:"post",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify(newFood)
    })
    .then(res=>res.json())
    .then(data=>{
      setFoods([...foods,data]);
      setNewFood({
        name:"",
        price:"",
        category:"",
        imageurl:""
      });
    })
    .catch(err => console.error("Error adding food:", err));
  };

  const deleteFood = (id) => {
    fetch(`${API_BASE_URL}/foods/${id}`, {
      method: "DELETE"
    }).then(() => {
      setFoods(foods.filter(food => food.id !== id));
    })
    .catch(err => console.error("Error deleting food:", err));
  };

const placeOrder = (food) => {
  setCart([...cart, food]);
};
const handlePayment = (method) => {
  setPaymentMethod(method);

  alert(`🎉 Payment Successful via ${method}! Order Confirmed.`);

  setCart([]);
  setShowPayment(false);
  setShowCart(false);
};

  return (
    <div className='container'>

      <div className="navbar">
        <h1 className="logo">🍔 SnackSpot</h1>
        <div>
          <span>Welcome {user}</span>
          <button onClick={() => setShowCart(!showCart)}>
    🛒 Cart ({cart.length})
  </button>
          <button onClick={onLogout}>Logout</button>
        </div>
      </div>

      {user === "bava" && (
        <>
          <h2>Add To SnackSpot</h2>
          <div className="form">
            <input name="name" placeholder='Food name' value={newFood.name} onChange={handleChange}/>
            <input name="price" placeholder='price' value={newFood.price} onChange={handleChange}/>
            <input name="category" placeholder='Category' value={newFood.category} onChange={handleChange}/>
            <input name="imageurl" placeholder='Image URL' value={newFood.imageurl} onChange={handleChange}/>
            <button onClick={addFood}>Add Food</button>
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
              fetch(`${API_BASE_URL}/foods/search?name=${value}`)
                .then(res => res.json())
                .then(data => setFoods(data))
                .catch(err => console.error("Error searching food:", err));
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

              {user === "bava" && (
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

    {cart.map((item, index) => (
      <div key={index} className="cart-item">
        <span>{item.name}</span>
        <span>₹{item.price}</span>
      </div>
    ))}

    <h3>
      Total: ₹
      {cart.reduce((total, item) => total + Number(item.price), 0)}
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

        <button onClick={() => handlePayment("UPI")}>
          Pay by UPI
        </button>

        <button onClick={() => handlePayment("COD")}>
          Cash on Delivery
        </button>
      </div>
    )}
  </div>
)}

    </div>
  );
}

export default Home;