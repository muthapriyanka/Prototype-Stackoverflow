import React from 'react';
import { Link } from 'react-router-dom';

function HomePage() {
  return (
    <div className="container mt-5">
      <h1>Welcome to StackOverflow Clone</h1>
      <Link to="/register" className="btn btn-primary me-2">Register</Link>
      <Link to="/login" className="btn btn-success">Login</Link>
      <Link to="/questions" className="btn btn-info ms-2">View Questions</Link>
    </div>
  );
}

export default HomePage;
