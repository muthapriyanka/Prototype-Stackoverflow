import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PostAnswer from "./PostAnswer";

function QuestionDetail({ user }) {
  const { id } = useParams();
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);

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

  if (loading) return <p>Loading...</p>;
  if (!question) return <p>Question not found</p>;

  return (
    <div className="container mt-4">
      <h2 className="mb-3">{question.title}</h2>

      <div className="card mb-4">
        <div className="card-body">
          <div className="d-flex align-items-start">
            <div className="me-4 text-center">
              <button className="btn btn-light">▲</button>
              <div>{question.voteCount}</div>
              <button className="btn btn-light">▼</button>
            </div>

            <div>
              <p>{question.body}</p>

              <div className="mt-2">
                {question.tags.map((tag) => (
                  <span key={tag} className="badge bg-secondary me-2">
                    {tag}
                  </span>
                ))}
              </div>

              <small className="text-muted">
                Asked on {new Date(question.createdAt).toLocaleString()}
              </small>
            </div>
          </div>
        </div>
      </div>

      <h4>{question.answers.length} Answers</h4>

      {question.answers.length === 0 ? (
        <p>No answers yet.</p>
      ) : (
        question.answers.map((ans) => (
          <div key={ans.id} className="card mb-3">
            <div className="card-body">
              <p>{ans.body}</p>
              <small className="text-muted">answered by {ans.username}</small>
            </div>
          </div>
        ))
      )}

      {user && (
        <PostAnswer
          questionId={question.id}
          user={user}
          onAnswerPosted={(newAnswer) => {
            setQuestion({
              ...question,
              answers: [...question.answers, newAnswer],
            });
          }}
        />
      )}
    </div>
  );
}

export default QuestionDetail;
