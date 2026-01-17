
import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import PostQuestion from "./PostQuestion";
import "./QuestionsPage.css";
import { getQuestionswithans } from "./services/api";

function QuestionsPage({ user, onLogout }) {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchQuestions();
  }, []);

  const fetchQuestions = async () => {
    try {
      const data = await getQuestionswithans(); // fetch questions (answers will be ignored here)
      if (!Array.isArray(data)) {
        console.error("Expected array, got:", data);
        setQuestions([]);
      } else {
        setQuestions(data);
      }
    } catch (err) {
      console.error("Failed to fetch questions:", err);
      setQuestions([]);
    }
    setLoading(false);
  };

  return (
    <div className="so-container">
      <h2 className="so-title">Top Questions</h2>

      {loading ? (
        <p>Loading questions...</p>
      ) : questions.length === 0 ? (
        <p>No questions yet.</p>
      ) : (
        questions.map((q) => (
          <div key={q.id} className="question-row mb-3">
            <div className="question-content">
              {/* Question Title */}
              <h5 className="question-title">
                <Link to={`/questions/${q.id}`}>{q.title}</Link>
              </h5>

              {/* Question Body */}
              <p className="question-body">{q.body}</p>

              {/* Tags */}
              {q.tags && q.tags.length && (
                <div className="question-tags mb-2">
                  {q.tags.map((tag) => (
                    <span
                      key={tag.id}
                      className="badge bg-secondary me-1"
                      style={{ fontSize: "0.85em" }}
                    >
                      {tag.name}
                    </span>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))
      )}

      {/* Post Question Form */}
      {user && (
        <PostQuestion
          onQuestionPosted={(newQ) => setQuestions([newQ, ...questions])}
        />
      )}
    </div>
  );
}

export default QuestionsPage;
