import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "react-bootstrap"; 
import "bootstrap/dist/css/bootstrap.min.css";

function NavBar({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate("/");
  };

  return (
    <div className="navbar d-flex justify-content-between align-items-center p-3 border-bottom">
      <Link to="/questions" className="navbar-brand">
        StackOverflow Clone
      </Link>

      <div>
        {user ? (
          <>
            <span className="me-3">Hi, {user.username}</span>
            <Button
              variant="outline-danger"
              className="ms-2"
              onClick={handleLogout}
            >
              Logout
            </Button>
          </>
        ) : (
          <>
            <Button onClick={() => navigate("/")}>Login</Button>
          </>
        )}
      </div>
    </div>
  );
}

export default NavBar;
