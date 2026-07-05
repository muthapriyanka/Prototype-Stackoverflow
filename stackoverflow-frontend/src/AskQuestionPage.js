import { Link, useNavigate } from "react-router-dom";
import PostQuestion from "./PostQuestion";
import Sidebar from "./Sidebar";
import "./QuestionsPage.css";

function AskQuestionPage({ user }) {
  const navigate = useNavigate();

  const handleQuestionPosted = (newQuestion) => {
    navigate(`/questions/${newQuestion.id}`);
  };

  return (
    <div className="so-shell">
      <Sidebar />

      <main className="so-main ask-page">
        <section className="ask-page-header">
          <h1>Ask a public question</h1>
          <Link to="/questions" className="outline-button">
            Back to questions
          </Link>
        </section>

        {user ? (
          <section className="ask-form-panel">
            <PostQuestion onQuestionPosted={handleQuestionPosted} />
          </section>
        ) : (
          <div className="feed-empty">
            Please login before asking a question.
          </div>
        )}
      </main>
    </div>
  );
}

export default AskQuestionPage;
