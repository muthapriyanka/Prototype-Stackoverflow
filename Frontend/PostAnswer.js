import { useState } from "react";

export default function PostAnswer({ questionId, onAnswerPosted }) {
  const [body, setBody] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const token = localStorage.getItem("token"); 
      if (!token) {
        alert("You must be logged in to post an answer");
        return;
      }

      const response = await fetch("http://localhost:8080/answers", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`, // 
        },
        body: JSON.stringify({
          body,
          questionId,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to post answer");
      }

      const newAnswer = await response.json();
      onAnswerPosted(newAnswer);
      setBody("");
    } catch (err) {
      console.error(err);
      alert(err.message);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="mt-2">
      <textarea
        value={body}
        onChange={(e) => setBody(e.target.value)}
        placeholder="Write your answer..."
        className="form-control mb-1"
        rows={2}
        required
      />
      <button type="submit" className="btn btn-sm btn-success">
        Post Answer
      </button>
    </form>
  );
}
