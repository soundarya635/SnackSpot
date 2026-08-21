import { useState, useEffect } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function AdminDashboard({ user, onLogout }) {
  const [activeTab, setActiveTab] = useState("foods"); // "foods" | "users"
  const [foods, setFoods] = useState([]);
  const [usersList, setUsersList] = useState([]);
  const [search, setSearch] = useState("");
  const [newFood, setNewFood] = useState({
    name: "",
    price: "",
    category: "",
    imageurl: "",
  });

  const fetchAllFoods = () => {
    fetch(`${API_BASE_URL}/foods`)
      .then((res) => res.json())
      .then((data) => setFoods(data))
      .catch((err) => console.error("Error fetching foods:", err));
  };

  const fetchAllUsers = () => {
    fetch(`${API_BASE_URL}/auth/users`)
      .then((res) => res.json())
      .then((data) => setUsersList(data))
      .catch((err) => console.error("Error fetching users:", err));
  };

  useEffect(() => {
    fetchAllFoods();
    fetchAllUsers();
  }, []);

  const handleFoodChange = (e) => {
    setNewFood({ ...newFood, [e.target.name]: e.target.value });
  };

  const addFood = (e) => {
    e.preventDefault();
    if (!newFood.name || !newFood.price || !newFood.category) {
      alert("Please fill in food name, price, and category.");
      return;
    }

    fetch(`${API_BASE_URL}/foods`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newFood),
    })
      .then((res) => res.json())
      .then((data) => {
        setFoods([...foods, data]);
        setNewFood({ name: "", price: "", category: "", imageurl: "" });
        alert("Food item added successfully! 🍔");
      })
      .catch((err) => console.error("Error adding food:", err));
  };

  const deleteFood = (id) => {
    if (!window.confirm("Are you sure you want to delete this food item?")) return;

    fetch(`${API_BASE_URL}/foods/${id}`, {
      method: "DELETE",
    })
      .then(() => {
        setFoods(foods.filter((food) => food.id !== id));
      })
      .catch((err) => console.error("Error deleting food:", err));
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
          <span>Welcome, <strong>{user}</strong></span>
          <button onClick={onLogout} className="logout-btn">Logout</button>
        </div>
      </div>

      {/* ADMIN TABS */}
      <div className="admin-tabs">
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
            <button type="submit">Add Food Item</button>
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
