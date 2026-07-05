import { useEffect, useState } from "react";
import Sidebar from "./Sidebar";
import { getTags } from "./services/api";
import "./QuestionsPage.css";

function TagsPage() {
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getTags()
      .then((data) => setTags(Array.isArray(data) ? data : []))
      .catch((err) => {
        console.error("Failed to load tags:", err);
        setTags([]);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="so-shell">
      <Sidebar active="tags" />

      <main className="so-main">
        <section className="feed-header">
          <div>
            <h1>Tags</h1>
            <p>{tags.length} tags</p>
          </div>
        </section>

        {loading ? (
          <div className="feed-empty">Loading tags...</div>
        ) : tags.length === 0 ? (
          <div className="feed-empty">No tags yet. Add tags while asking a question.</div>
        ) : (
          <div className="directory-grid">
            {tags.map((tag) => (
              <article key={tag.id} className="directory-card">
                <span className="tag-pill">{tag.name}</span>
                <p>{tag.questionCount ?? 0} questions</p>
              </article>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

export default TagsPage;
