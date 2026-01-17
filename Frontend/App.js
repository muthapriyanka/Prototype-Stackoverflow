import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import AuthPage from "./AuthPage";
import QuestionsPage from "./QuestionsPage";
import 'bootstrap/dist/css/bootstrap.min.css';
import PostQuestion from "./PostQuestion";
import './App.css';
import NavBar from "./Navbar";
import QuestionDetail from "./QuestionDetailPage";

function App() {

  const [user, setUser] = useState(null);

  // Restore login from localStorage
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handleLogin = (loggedUser) => {
    setUser(loggedUser);
    localStorage.setItem("user", JSON.stringify(loggedUser));
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem("user");
    localStorage.removeItem("token");  
  };

  return (
    <Router>
    {user && <NavBar user={user} onLogout={handleLogout} />}
      <Routes>
        <Route path="/" element={<AuthPage onLogin={handleLogin}/>} />
        <Route path="/ask" element={<PostQuestion user={user} />} />
        <Route path="/questions" element={<QuestionsPage user={user} onLogout={handleLogout} />} />
         <Route path="/questions/:id" element={<QuestionDetail user={user} />}/>
      </Routes>
    </Router>
  );
}

export default App;
