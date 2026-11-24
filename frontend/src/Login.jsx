import { useState } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function Login({ onLogin }) {
  // mode can be: "user-login" | "user-register" | "admin-login"
  const [mode, setMode] = useState("user-login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage("");

    const trimmedUsername = username.trim();
    const trimmedPassword = password.trim();

    if (!trimmedUsername || !trimmedPassword) {
      setErrorMessage("Please enter both username and password");
      return;
    }

    if (mode === "user-register") {
      if (trimmedUsername.length < 3) {
        setErrorMessage("Username must be at least 3 characters long");
        return;
      }
      if (trimmedPassword.length < 6) {
        setErrorMessage("Password must be at least 6 characters long");
        return;
      }
    }

    setLoading(true);

    let endpoint = `${API_BASE_URL}/auth/login`;
    if (mode === "user-register") {
      endpoint = `${API_BASE_URL}/auth/register`;
    } else if (mode === "admin-login") {
      endpoint = `${API_BASE_URL}/auth/admin/login`;
    }

    fetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username: trimmedUsername, password: trimmedPassword }),
    })
      .then(async (res) => {
        const data = await res.json().catch(() => ({}));
        if (!res.ok) {
          throw new Error(data.message || (res.status === 409 ? "Username already exists!" : res.status === 401 ? "Invalid username or password" : res.status === 403 ? "Access Denied: Not an Admin account" : "Authentication failed"));
        }
        return data;
      })
      .then((data) => {
        setLoading(false);
        if (data.success) {
          if (mode === "user-register") {
            alert(data.message || "User registered successfully!");
            setMode("user-login");
            setPassword("");
          } else {
            const userData = {
              username: data.username || trimmedUsername,
              role: data.role || (mode === "admin-login" ? "ADMIN" : "USER"),
              token: data.token,
            };
            localStorage.setItem("user", JSON.stringify(userData));
            onLogin(userData);
          }
        } else {
          setErrorMessage(data.message || "Authentication failed.");
        }
      })
      .catch((err) => {
        setLoading(false);
        console.error("Auth error:", err);
        setErrorMessage(err.message || "Failed to connect to server. Ensure Spring Boot backend is running.");
      });
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2 className="brand-logo">🍔 SnackSpot</h2>
        
        {/* LOGIN MODE TABS */}
        <div className="auth-tabs">
          <button
            type="button"
            className={mode === "user-login" ? "auth-tab active" : "auth-tab"}
            onClick={() => { setMode("user-login"); setErrorMessage(""); }}
          >
            User Login
          </button>
          <button
            type="button"
            className={mode === "user-register" ? "auth-tab active" : "auth-tab"}
            onClick={() => { setMode("user-register"); setErrorMessage(""); }}
          >
            Register
          </button>
          <button
            type="button"
            className={mode === "admin-login" ? "auth-tab active admin-tab" : "auth-tab admin-tab"}
            onClick={() => { setMode("admin-login"); setErrorMessage(""); }}
          >
            🛡️ Admin Login
          </button>
        </div>

        <h3 className="auth-title">
          {mode === "user-login" && "Welcome Back!"}
          {mode === "user-register" && "Create User Account"}
          {mode === "admin-login" && "Admin Portal Access"}
        </h3>

        {errorMessage && <div className="error-alert">{errorMessage}</div>}

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={loading}
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
          />

          <button type="submit" disabled={loading} className="submit-btn">
            {loading ? "Processing..." : mode === "user-register" ? "Sign Up" : "Login"}
          </button>
        </form>

        <div className="auth-footer-note">
          {mode === "admin-login" ? (
            <p>Admin accounts require authorized access.</p>
          ) : mode === "user-register" ? (
            <p>Already registered? <span onClick={() => setMode("user-login")}>Sign in here</span></p>
          ) : (
            <p>Need an account? <span onClick={() => setMode("user-register")}>Register here</span></p>
          )}
        </div>
      </div>
    </div>
  );
}

export default Login;
