const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const getStoredUser = () => {
  const saved = localStorage.getItem("user");
  if (!saved) return null;
  try {
    return JSON.parse(saved);
  } catch (e) {
    return { username: saved, role: "USER" };
  }
};

export const getAuthToken = () => {
  const user = getStoredUser();
  return user?.token || null;
};

export const clearStoredUser = () => {
  localStorage.removeItem("user");
};

export const authFetch = async (url, options = {}) => {
  const token = getAuthToken();
  const headers = {
    ...options.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    // Expired or invalid token
    clearStoredUser();
    window.location.reload();
  }

  return response;
};

export default API_BASE_URL;
