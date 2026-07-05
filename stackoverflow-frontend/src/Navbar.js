import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

function NavBar({ user, onLogout }) {
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState("");

  const handleLogout = () => {
    onLogout();
    navigate("/");
  };

  const handleSearchSubmit = (event) => {
    event.preventDefault();
    const trimmed = searchText.trim();
    if (!trimmed) {
      navigate("/questions");
      return;
    }

    navigate(`/questions?q=${encodeURIComponent(trimmed)}`);
  };

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <Link to="/questions" className="brand-mark">
          <span className="brand-icon">SO</span>
          <span>Prototype of Stackoverflow</span>
        </Link>

        <nav className="topbar-nav">
          <Link to="/questions">Questions</Link>
          {user && <Link to="/ask">Ask</Link>}
        </nav>

        <form className="topbar-search" onSubmit={handleSearchSubmit}>
          <input
            type="search"
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            placeholder="Search questions"
            aria-label="Search questions"
          />
        </form>

        <div className="topbar-actions">
          {user ? (
            <>
              <span className="user-chip">{user.username}</span>
              <button className="outline-button" onClick={handleLogout} type="button">
                Logout
              </button>
            </>
          ) : (
            <button className="outline-button" onClick={() => navigate("/")} type="button">
              Login
            </button>
          )}
        </div>
      </div>
    </header>
  );
}

export default NavBar;
