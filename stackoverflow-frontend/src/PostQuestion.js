import React, { useEffect, useMemo, useState } from "react";

function PostQuestion({ onQuestionPosted = () => {} }) {
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [availableTags, setAvailableTags] = useState([]);
  const [tagInput, setTagInput] = useState("");

  useEffect(() => {
    fetch("http://localhost:8080/tags")
      .then((res) => res.json())
      .then((data) => setAvailableTags(Array.isArray(data) ? data : []))
      .catch((err) => console.error(err));
  }, []);

  const parsedTags = useMemo(() => {
    const seen = new Set();
    return tagInput
      .split(/[\s,]+/)
      .map((tag) => tag.trim().toLowerCase().replace(/\s+/g, "-"))
      .filter(Boolean)
      .filter((tag) => {
        if (seen.has(tag)) {
          return false;
        }
        seen.add(tag);
        return true;
      })
      .slice(0, 5);
  }, [tagInput]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const token = localStorage.getItem("token");

      if (!token) {
        alert("You must be logged in to post a question");
        return;
      }

      const bodyToSend = {
        title,
        body,
        tagNames: parsedTags,
      };

      const response = await fetch("http://localhost:8080/questions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(bodyToSend),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to post question");
      }

      const newQuestion = await response.json();
      onQuestionPosted(newQuestion);

      setTitle("");
      setBody("");
      setTagInput("");
    } catch (err) {
      console.error(err);
      alert(err.message);
    }
  };

  return (
    <div className="post-question">
      <h4>Ask a Question</h4>
      <form onSubmit={handleSubmit}>
        <label className="form-label">Title</label>
        <input
          type="text"
          placeholder="e.g. How do I use Kafka with Spring Boot?"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="form-control mb-2"
          required
        />

        <label className="form-label">Body</label>
        <textarea
          placeholder="Include what you tried and what you expected to happen"
          value={body}
          onChange={(e) => setBody(e.target.value)}
          className="form-control mb-2"
          rows={6}
          required
        />

        <label className="form-label">Tags</label>
        <input
          type="text"
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
          className="form-control mb-2"
          placeholder="java spring-boot kafka"
          list="available-tags"
        />
        <datalist id="available-tags">
          {availableTags.map((tag) => (
            <option key={tag.id} value={tag.name} />
          ))}
        </datalist>

        {parsedTags.length > 0 && (
          <div className="summary-tags tag-preview">
            {parsedTags.map((tag) => (
              <span key={tag} className="tag-pill">
                {tag}
              </span>
            ))}
          </div>
        )}

        <button className="btn btn-primary">Post Question</button>
      </form>
    </div>
  );
}

export default PostQuestion;
