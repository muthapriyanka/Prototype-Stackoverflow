import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PostAnswer from "./PostAnswer";
import Sidebar from "./Sidebar";
import "./QuestionsPage.css";

function QuestionDetail({ user }) {
  const { id } = useParams();
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [userVote, setUserVote] = useState(null);
  const [voteSubmitting, setVoteSubmitting] = useState(false);

  useEffect(() => {
    fetch(`http://localhost:8080/questions/${id}/detail`)
      .then((res) => {
        if (!res.ok) throw new Error("Failed to load question");
        return res.json();
      })
      .then((data) => setQuestion(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [id]);

  const handleVote = async (value) => {
    if (!user) {
      alert("You must be logged in to vote");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      alert("You must be logged in to vote");
      return;
    }

    setVoteSubmitting(true);
    try {
      const response = await fetch("http://localhost:8080/votes", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          entityId: id,
          entityType: "QUESTION",
          value,
        }),
      });

      if (!response.ok) {
        let message = "Failed to vote";
        try {
          const error = await response.json();
          message = error.message || error.error || message;
        } catch {
          // Keep fallback message when the backend does not return JSON.
        }
        throw new Error(message);
      }

      const data = await response.json();
      setUserVote(data.userVote);
      setQuestion((current) => ({
        ...current,
        voteCount: data.voteCount,
      }));
    } catch (err) {
      console.error(err);
      alert(err.message);
    } finally {
      setVoteSubmitting(false);
    }
  };

  if (loading) return <div className="so-main standalone">Loading...</div>;
  if (!question) return <div className="so-main standalone">Question not found</div>;

  const sortedAnswers = [...(question.answers || [])].sort((a, b) => {
    const scoreDiff = (b.voteCount ?? 0) - (a.voteCount ?? 0);
    if (scoreDiff !== 0) return scoreDiff;
    return new Date(a.createdAt || 0) - new Date(b.createdAt || 0);
  });

  return (
    <div className="so-shell">
      <Sidebar active="questions" />

      <main className="so-main question-detail">
        <section className="detail-header">
          <h1>{question.title}</h1>
          <div className="detail-meta">
            Asked {new Date(question.createdAt).toLocaleString()}
          </div>
        </section>

        <article className="post-layout">
          <div className="vote-column">
            <button
              className={`vote-button ${userVote === 1 ? "active" : ""}`}
              disabled={voteSubmitting}
              onClick={() => handleVote(1)}
              type="button"
            >
              ▲
            </button>
            <div className="vote-score">{question.voteCount}</div>
            <button
              className={`vote-button ${userVote === -1 ? "active" : ""}`}
              disabled={voteSubmitting}
              onClick={() => handleVote(-1)}
              type="button"
            >
              ▼
            </button>
          </div>

          <div className="post-content">
            <p>{question.body}</p>

            <div className="tag-row">
              {(question.tags || []).map((tag) => (
                <span key={tag} className="tag-pill">
                  {tag}
                </span>
              ))}
            </div>
          </div>
        </article>

        <h2 className="answers-title">{sortedAnswers.length} Answers</h2>

        {sortedAnswers.length === 0 ? (
          <div className="feed-empty">No answers yet.</div>
        ) : (
          sortedAnswers.map((ans) => (
            <article key={ans.id} className="answer-summary">
              <div className="answer-score">
                <strong>{ans.voteCount ?? 0}</strong>
                <span>score</span>
              </div>
              <div className="answer-content">
                <p>{ans.body}</p>
                <div className="question-meta">answered by {ans.username}</div>
              </div>
            </article>
          ))
        )}

        {user && (
          <section className="ask-panel">
            <PostAnswer
              questionId={question.id}
              user={user}
              onAnswerPosted={(newAnswer) => {
                setQuestion({
                  ...question,
                  answers: [...(question.answers || []), newAnswer],
                });
              }}
            />
          </section>
        )}
      </main>
    </div>
  );
}

export default QuestionDetail;
