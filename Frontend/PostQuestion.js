import React, { useEffect, useState } from "react";

function PostQuestion({ onQuestionPosted }) {
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [tags, setTags] = useState([]);
  const [selectedTag, setSelectedTag] = useState(""); 

  useEffect(() => {
    fetch("http://localhost:8080/tags")
      .then(res => res.json())
      .then(data => setTags(data))
      .catch(err => console.error(err));
  }, []);

  const handleTagChange = (e) => {
    setSelectedTag(e.target.value); // store single value
  };

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
        tagIds: selectedTag ? [selectedTag] : [] // send as array
      };
    console.log("Posting question JSON:", JSON.stringify(bodyToSend));

      const response = await fetch("http://localhost:8080/questions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(bodyToSend)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to post question");
      }

      const newQuestion = await response.json();
      onQuestionPosted(newQuestion);

      // reset form
      setTitle("");
      setBody("");
      setSelectedTag("");

    } catch (err) {
      console.error(err);
      alert(err.message);
    }
  };

  return (
    <div className="post-question">
      <h4>Ask a Question</h4>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Question title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="form-control mb-2"
          required
        />
        <textarea
          placeholder="Question details"
          value={body}
          onChange={(e) => setBody(e.target.value)}
          className="form-control mb-2"
          rows={3}
          required
        />

        <select
          value={selectedTag}
          onChange={handleTagChange}
          className="form-control mb-2"
        >
          <option value="">-- Select a tag --</option>
          {tags.map(tag => (
            <option key={tag.id} value={tag.id}>
              {tag.name}
            </option>
          ))}
        </select>

        <button className="btn btn-primary">Post Question</button>
      </form>
    </div>
  );
}

export default PostQuestion;
