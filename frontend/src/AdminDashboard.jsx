import { useState, useEffect } from "react";
import { authFetch } from "./api";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function AdminDashboard({ user, onLogout }) {
  const [activeTab, setActiveTab] = useState("orders"); // "orders" | "foods" | "users"
  const [orders, setOrders] = useState([]);
  const [foods, setFoods] = useState([]);
  const [usersList, setUsersList] = useState([]);
  const [search, setSearch] = useState("");
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [newFood, setNewFood] = useState({
    name: "",
    price: "",
    category: "",
    imageurl: "",
  });
  const [isAddingFood, setIsAddingFood] = useState(false);

  const fetchAllOrders = () => {
    authFetch(`${API_BASE_URL}/admin/orders`)
      .then(async (res) => {
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || "Failed to fetch customer orders");
        }
        return res.json();
      })
      .then((data) => setOrders(Array.isArray(data) ? data : []))
      .catch((err) => console.error("Error fetching orders:", err));
  };

  const fetchAllFoods = () => {
    fetch(`${API_BASE_URL}/foods`)
      .then(async (res) => {
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || "Failed to fetch foods");
        }
        return res.json();
      })
      .then((data) => setFoods(Array.isArray(data) ? data : []))
      .catch((err) => console.error("Error fetching foods:", err));
  };

  const fetchAllUsers = () => {
    authFetch(`${API_BASE_URL}/auth/users`)
      .then(async (res) => {
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.message || "Failed to fetch users");
        }
        return res.json();
      })
      .then((data) => setUsersList(Array.isArray(data) ? data : []))
      .catch((err) => console.error("Error fetching users:", err));
  };

  useEffect(() => {
    fetchAllOrders();
    fetchAllFoods();
    fetchAllUsers();
  }, []);

  const handleStatusChange = (orderId, newStatus) => {
    setUpdatingOrderId(orderId);
    authFetch(`${API_BASE_URL}/admin/orders/${orderId}/status`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: newStatus }),
    })
      .then(async (res) => {
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          throw new Error(data.message || "Failed to update order status");
        }
        return data;
      })
      .then((updatedOrder) => {
        setUpdatingOrderId(null);
        setOrders(orders.map((o) => (o.id === orderId ? updatedOrder : o)));
        alert(`Order #${orderId} status changed to ${newStatus}`);
      })
      .catch((err) => {
        setUpdatingOrderId(null);
        console.error("Status update error:", err);
        alert(err.message || "Error updating order status");
      });
  };

  const handleFoodChange = (e) => {
    setNewFood({ ...newFood, [e.target.name]: e.target.value });
  };

  const addFood = (e) => {
    e.preventDefault();
    if (!newFood.name.trim() || !newFood.price || !newFood.category.trim()) {
      alert("Please fill in food name, price, and category.");
      return;
    }

    const parsedPrice = parseFloat(newFood.price);
    if (isNaN(parsedPrice) || parsedPrice <= 0) {
      alert("Price must be a valid number greater than zero.");
      return;
    }

    setIsAddingFood(true);

    authFetch(`${API_BASE_URL}/foods`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ...newFood,
        price: parsedPrice,
      }),
    })
      .then(async (res) => {
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          throw new Error(data.message || (data.errors ? Object.values(data.errors).join(", ") : "Failed to add food"));
        }
        return data;
      })
      .then((data) => {
        setIsAddingFood(false);
        setFoods([...foods, data]);
        setNewFood({ name: "", price: "", category: "", imageurl: "" });
        alert("Food item added successfully! 🍔");
      })
      .catch((err) => {
        setIsAddingFood(false);
        console.error("Error adding food:", err);
        alert(err.message || "Error adding food");
      });
  };

  const deleteFood = (id) => {
    if (!window.confirm("Are you sure you want to delete this food item?")) return;

    authFetch(`${API_BASE_URL}/foods/${id}`, {
      method: "DELETE",
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("Failed to delete food item");
        }
        setFoods(foods.filter((food) => food.id !== id));
      })
      .catch((err) => {
        console.error("Error deleting food:", err);
        alert(err.message || "Error deleting food");
      });
  };

  return (
    <div className="container">
      {/* NAVBAR */}
      <div className="navbar admin-navbar">
        <div className="logo-section">
          <h1 className="logo">🛡️ Admin Dashboard</h1>
          <span className="admin-badge">ADMIN MODE</span>
        </div>
        <div>
          <span>Welcome, <strong>{typeof user === 'object' ? user.username : user}</strong></span>
          <button onClick={onLogout} className="logout-btn">Logout</button>
        </div>
      </div>

      {/* ADMIN TABS */}
      <div className="admin-tabs">
        <button
          className={activeTab === "orders" ? "tab-btn active" : "tab-btn"}
          onClick={() => { setActiveTab("orders"); fetchAllOrders(); }}
        >
          📦 Customer Orders ({orders.length})
        </button>
        <button
          className={activeTab === "foods" ? "tab-btn active" : "tab-btn"}
          onClick={() => setActiveTab("foods")}
        >
          🍔 Manage Food Menu ({foods.length})
        </button>
        <button
          className={activeTab === "users" ? "tab-btn active" : "tab-btn"}
          onClick={() => setActiveTab("users")}
        >
          👥 Registered Users ({usersList.length})
        </button>
      </div>

      {/* ORDERS MANAGEMENT TAB */}
      {activeTab === "orders" && (
        <div className="tab-content">
          <h2>Customer Orders</h2>
          {orders.length === 0 ? (
            <p>No customer orders placed yet.</p>
          ) : (
            <div className="users-table-container">
              <table className="users-table">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Customer</th>
                    <th>Items</th>
                    <th>Total</th>
                    <th>Payment</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => (
                    <tr key={order.id}>
                      <td><strong>#{order.id}</strong></td>
                      <td>{order.username}</td>
                      <td>
                        <ul style={{ margin: 0, paddingLeft: "16px", textAlign: "left" }}>
                          {order.items && order.items.map((it) => (
                            <li key={it.id}>
                              {it.foodName} x {it.quantity} (₹{it.price})
                            </li>
                          ))}
                        </ul>
                      </td>
                      <td><strong>₹{order.totalAmount}</strong></td>
                      <td>{order.paymentMethod}</td>
                      <td><small>{order.createdAt ? new Date(order.createdAt).toLocaleDateString() : ""}</small></td>
                      <td>
                        <span className={`role-badge ${order.orderStatus}`}>
                          {order.orderStatus}
                        </span>
                      </td>
                      <td>
                        <select
                          value={order.orderStatus}
                          disabled={updatingOrderId === order.id || order.orderStatus === 'DELIVERED' || order.orderStatus === 'CANCELLED'}
                          onChange={(e) => handleStatusChange(order.id, e.target.value)}
                          style={{ padding: "4px", borderRadius: "4px" }}
                        >
                          <option value="PENDING">PENDING</option>
                          <option value="CONFIRMED">CONFIRMED</option>
                          <option value="PREPARING">PREPARING</option>
                          <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>
                          <option value="DELIVERED">DELIVERED</option>
                          <option value="CANCELLED">CANCELLED</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* FOOD MANAGEMENT TAB */}
      {activeTab === "foods" && (
        <div className="tab-content">
          <h2>Add New Food Item</h2>
          <form className="form" onSubmit={addFood}>
            <input
              name="name"
              placeholder="Food name"
              value={newFood.name}
              onChange={handleFoodChange}
            />
            <input
              name="price"
              placeholder="Price (₹)"
              type="number"
              value={newFood.price}
              onChange={handleFoodChange}
            />
            <input
              name="category"
              placeholder="Category (e.g. Snacks, Drinks, Pizza)"
              value={newFood.category}
              onChange={handleFoodChange}
            />
            <input
              name="imageurl"
              placeholder="Image URL"
              value={newFood.imageurl}
              onChange={handleFoodChange}
            />
            <button type="submit" disabled={isAddingFood}>
              {isAddingFood ? "Adding..." : "Add Food Item"}
            </button>
          </form>

          <h2>Current Menu Items</h2>
          <div className="searchBox">
            <input
              type="text"
              placeholder="Search items..."
              value={search}
              onChange={(e) => {
                const value = e.target.value;
                setSearch(value);
                if (value.trim() === "") {
                  fetchAllFoods();
                } else {
                  fetch(`${API_BASE_URL}/foods/search?name=${value}`)
                    .then((res) => res.json())
                    .then((data) => setFoods(data));
                }
              }}
            />
          </div>

          <div className="grid">
            {foods.map((food) => (
              <div className="card admin-card" key={food.id}>
                <img
                  src={food.imageurl || "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=300&q=80"}
                  alt={food.name}
                />
                <h3>{food.name}</h3>
                <p>₹{food.price}</p>
                <span>{food.category}</span>
                <div className="buttons">
                  <button className="delete" onClick={() => deleteFood(food.id)}>
                    🗑️ Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* USER MANAGEMENT TAB */}
      {activeTab === "users" && (
        <div className="tab-content">
          <h2>Registered Application Users</h2>
          <div className="users-table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Role</th>
                </tr>
              </thead>
              <tbody>
                {usersList.map((u) => (
                  <tr key={u.id}>
                    <td>#{u.id}</td>
                    <td><strong>{u.username}</strong></td>
                    <td>
                      <span className={`role-badge ${u.role}`}>{u.role}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;
