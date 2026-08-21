import { useState, useEffect } from "react";
import Login from "./Login";
import Home from "./Home";
import AdminDashboard from "./AdminDashboard";
import "./App.css";

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const savedUser = localStorage.getItem("user");
    if (savedUser) {
      try {
        const parsed = JSON.parse(savedUser);
        if (parsed && parsed.username) {
          setUser(parsed);
        } else {
          setUser({ username: savedUser, role: "USER" });
        }
      } catch (e) {
        setUser({ username: savedUser, role: "USER" });
      }
    }
  }, []);

  const handleLogin = (userData) => {
    if (typeof userData === "string") {
      setUser({ username: userData, role: "USER" });
    } else {
      setUser(userData);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("user");
    setUser(null);
  };

  if (!user) {
    return <Login onLogin={handleLogin} />;
  }

  if (user.role === "ADMIN") {
    return <AdminDashboard user={user.username} onLogout={handleLogout} />;
  }

  return <Home user={user.username} onLogout={handleLogout} />;
}

export default App;