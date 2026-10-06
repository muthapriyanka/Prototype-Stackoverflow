import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PostAnswer from "./PostAnswer";
import Sidebar from "./Sidebar";
import { API_BASE_URL } from "./services/api";
import "./QuestionsPage.css";

function QuestionDetail({ user }) {
  const { id } = useParams();
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [userVote, setUserVote] = useState(null);
  const [voteSubmitting, setVoteSubmitting] = useState(false);
  const [answerVotes, setAnswerVotes] = useState({});
  const [answerVoteSubmitting, setAnswerVoteSubmitting] = useState(null);
  const [commentDrafts, setCommentDrafts] = useState({});
  const [commentSubmitting, setCommentSubmitting] = useState(null);

  const loadQuestion = useCallback(() => {
    setLoading(true);
    fetch(`${API_BASE_URL}/questions/${id}/detail`)
      .then((res) => {
        if (!res.ok) throw new Error("Failed to load question");
        return res.json();
      })
      .then((data) => setQuestion(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    loadQuestion();
  }, [loadQuestion]);

  const getAuthToken = (action) => {
    if (!user) {
      alert(`You must be logged in to ${action}`);
      return null;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      alert(`You must be logged in to ${action}`);
      return null;
    }

    return token;
  };

  const submitVote = async (entityId, entityType, value) => {
    const token = getAuthToken("vote");
    if (!token) {
      return null;
    }

    const response = await fetch(`${API_BASE_URL}/votes`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        entityId,
        entityType,
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

    return response.json();
  };

  const handleQuestionVote = async (value) => {
    setVoteSubmitting(true);
    try {
      const data = await submitVote(id, "QUESTION", value);
      if (!data) {
        return;
      }

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

  const handleAnswerVote = async (answerId, value) => {
    setAnswerVoteSubmitting(answerId);
    try {
      const data = await submitVote(answerId, "ANSWER", value);
      if (!data) {
        return;
      }

      setAnswerVotes((current) => ({
        ...current,
        [answerId]: data.userVote,
      }));
      setQuestion((current) => ({
        ...current,
        answers: (current.answers || []).map((answer) =>
          answer.id === answerId
            ? { ...answer, voteCount: data.voteCount }
            : answer
        ),
      }));
    } catch (err) {
      console.error(err);
      alert(err.message);
    } finally {
      setAnswerVoteSubmitting(null);
    }
  };

  const updateCommentDraft = (targetKey, value) => {
    setCommentDrafts((current) => ({
      ...current,
      [targetKey]: value,
    }));
  };

  const handleCommentSubmit = async (event, targetKey, payload) => {
    event.preventDefault();

    const body = (commentDrafts[targetKey] || "").trim();
    if (!body) {
      return;
    }

    const token = getAuthToken("comment");
    if (!token) {
      return;
    }

    setCommentSubmitting(targetKey);
    try {
      const response = await fetch(`${API_BASE_URL}/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          body,
          ...payload,
        }),
      });

      if (!response.ok) {
        let message = "Failed to add comment";
        try {
          const error = await response.json();
          message = error.message || error.error || message;
        } catch {
          // Keep fallback message when the backend does not return JSON.
        }
        throw new Error(message);
      }

      const comment = await response.json();
      setQuestion((current) => {
        if (!current) {
          return current;
        }

        if (payload.questionId) {
          return {
            ...current,
            comments: [...(current.comments || []), comment],
          };
        }

        return {
          ...current,
          answers: (current.answers || []).map((answer) =>
            answer.id === payload.answerId
              ? {
                  ...answer,
                  comments: [...(answer.comments || []), comment],
                }
              : answer
          ),
        };
      });
      setCommentDrafts((current) => ({
        ...current,
        [targetKey]: "",
      }));
    } catch (err) {
      console.error(err);
      alert(err.message);
    } finally {
      setCommentSubmitting(null);
    }
  };

  if (loading) return <div className="so-main standalone">Loading...</div>;
  if (!question) return <div className="so-main standalone">Question not found</div>;

  const sortedAnswers = [...(question.answers || [])].sort((a, b) => {
    const scoreDiff = (b.voteCount ?? 0) - (a.voteCount ?? 0);
    if (scoreDiff !== 0) return scoreDiff;
    return new Date(a.createdAt || 0) - new Date(b.createdAt || 0);
  });

  const renderComments = (comments = []) =>
    comments.length > 0 && (
      <div className="comments-list">
        {comments.map((comment) => (
          <div key={comment.id} className="comment-item">
            <span>{comment.body}</span>
            <span className="comment-meta">
              {comment.username} · {new Date(comment.createdAt).toLocaleString()}
            </span>
          </div>
        ))}
      </div>
    );

  const renderCommentForm = (targetKey, payload) =>
    user && (
      <form
        className="comment-form"
        onSubmit={(event) => handleCommentSubmit(event, targetKey, payload)}
      >
        <input
          className="comment-input"
          value={commentDrafts[targetKey] || ""}
          onChange={(event) => updateCommentDraft(targetKey, event.target.value)}
          placeholder="Add a comment"
        />
        <button
          className="comment-button"
          disabled={commentSubmitting === targetKey}
          type="submit"
        >
          Comment
        </button>
      </form>
    );

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
              onClick={() => handleQuestionVote(1)}
              type="button"
            >
              ▲
            </button>
            <div className="vote-score">{question.voteCount}</div>
            <button
              className={`vote-button ${userVote === -1 ? "active" : ""}`}
              disabled={voteSubmitting}
              onClick={() => handleQuestionVote(-1)}
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

            {renderComments(question.comments || [])}
            {renderCommentForm(`question:${question.id}`, {
              questionId: question.id,
            })}
          </div>
        </article>

        <h2 className="answers-title">{sortedAnswers.length} Answers</h2>

        {sortedAnswers.length === 0 ? (
          <div className="feed-empty">No answers yet.</div>
        ) : (
          sortedAnswers.map((ans) => (
            <article key={ans.id} className="answer-summary">
              <div className="answer-vote-column">
                <button
                  className={`vote-button compact ${
                    answerVotes[ans.id] === 1 ? "active" : ""
                  }`}
                  disabled={answerVoteSubmitting === ans.id}
                  onClick={() => handleAnswerVote(ans.id, 1)}
                  type="button"
                >
                  ▲
                </button>
                <strong>{ans.voteCount ?? 0}</strong>
                <span>score</span>
                <button
                  className={`vote-button compact ${
                    answerVotes[ans.id] === -1 ? "active" : ""
                  }`}
                  disabled={answerVoteSubmitting === ans.id}
                  onClick={() => handleAnswerVote(ans.id, -1)}
                  type="button"
                >
                  ▼
                </button>
              </div>
              <div className="answer-content">
                <p>{ans.body}</p>
                {renderComments(ans.comments || [])}
                {renderCommentForm(`answer:${ans.id}`, {
                  answerId: ans.id,
                })}
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
              onAnswerPosted={loadQuestion}
            />
          </section>
        )}
      </main>
    </div>
  );
}

export default QuestionDetail;
