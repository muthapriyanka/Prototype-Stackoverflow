import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";
import AskQuestionPage from "./AskQuestionPage";
import AuthPage from "./AuthPage";
import NavBar from "./Navbar";
import QuestionDetail from "./QuestionDetailPage";
import QuestionsPage from "./QuestionsPage";
import TagsPage from "./TagsPage";
import UsersPage from "./UsersPage";

function App() {
  const [user, setUser] = useState(null);

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
      <NavBar user={user} onLogout={handleLogout} />
      <div className="app-body">
        <Routes>
          <Route path="/" element={<AuthPage onLogin={handleLogin} />} />
          <Route path="/ask" element={<AskQuestionPage user={user} />} />
          <Route path="/questions" element={<QuestionsPage user={user} />} />
          <Route path="/questions/:id/:slug" element={<QuestionDetail user={user} />} />
          <Route path="/questions/:id" element={<QuestionDetail user={user} />} />
          <Route path="/tags" element={<TagsPage />} />
          <Route path="/users" element={<UsersPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
