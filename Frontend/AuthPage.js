import React, { useState } from "react";
import { api } from "./services/api";
import "bootstrap/dist/css/bootstrap.min.css";
import { useNavigate } from "react-router-dom";

function AuthPage({ onLogin }) {
    const navigate = useNavigate(); 

  const [isLogin, setIsLogin] = useState(true); // toggle between login/signup
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    try {
      if (isLogin) {
        // LOGIN
        const res = await api.post("/users/login", {
          username: formData.username,
          password: formData.password,
        });
        localStorage.setItem("token", res.data.token);
        onLogin(res.data.user);
        navigate("/questions");
        setFormData({ username: "", password: "", email: "" });
      } else {
        // SIGNUP
        await api.post("/users", formData);
        alert("Signup successful! Please login.");
        setIsLogin(true);
        setFormData({ username: "", password: "", email: "" });
      }
    } catch (err) {
      console.error(err.response?.data || err.message);
      alert("Error: " + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="auth-container container mt-4 p-4 border rounded">
      <h3 className="mb-3">{isLogin ? "Login" : "Signup"}</h3>

      <input
        type="text"
        name="username"
        placeholder="Username"
        className="form-control mb-2"
        value={formData.username}
        onChange={handleChange}
      />

      {!isLogin && (
        <input
          type="email"
          name="email"
          placeholder="Email"
          className="form-control mb-2"
          value={formData.email}
          onChange={handleChange}
        />
      )}

      <input
        type="password"
        name="password"
        placeholder="Password"
        className="form-control mb-2"
        value={formData.password}
        onChange={handleChange}
      />

      <button className="btn btn-primary w-100 mb-2" onClick={handleSubmit}>
        {isLogin ? "Login" : "Signup"}
      </button>

      <div className="text-center">
        {isLogin ? (
          <p>
            Don't have an account?{" "}
            <button
              className="btn btn-link p-0"
              onClick={() => setIsLogin(false)}
            >
              Signup
            </button>
          </p>
        ) : (
          <p>
            Already have an account?{" "}
            <button
              className="btn btn-link p-0"
              onClick={() => setIsLogin(true)}
            >
              Login
            </button>
          </p>
        )}
      </div>
    </div>
  );
}

export default AuthPage;
