import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Sidebar from "./Sidebar";
import "./QuestionsPage.css";
import { getFeed, searchQuestions } from "./services/api";

const feedTabs = [
  { key: "active", label: "Active" },
  { key: "newest", label: "Newest" },
  { key: "score", label: "Score" },
];

const searchTabs = [
  { key: "relevance", label: "Relevance" },
  { key: "newest", label: "Newest" },
  { key: "score", label: "Score" },
];

function QuestionsPage({ user }) {
  const [searchParams] = useSearchParams();
  const searchQuery = (searchParams.get("q") || "").trim();
  const isSearchMode = searchQuery.length > 0;
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sort, setSort] = useState("active");
  const activeTabs = isSearchMode ? searchTabs : feedTabs;
  const effectiveSort = isSearchMode
    ? (sort === "active" ? "relevance" : sort)
    : (sort === "relevance" ? "active" : sort);

  useEffect(() => {
    const fetchQuestions = async () => {
      setLoading(true);
      try {
        const data = isSearchMode
          ? await searchQuestions(searchQuery, effectiveSort)
          : await getFeed(effectiveSort);

        if (!Array.isArray(data)) {
          console.error("Expected array, got:", data);
          setQuestions([]);
        } else {
          setQuestions(data);
        }
      } catch (err) {
        console.error("Failed to fetch questions:", err);
        setQuestions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchQuestions();
  }, [isSearchMode, searchQuery, effectiveSort]);

  const formatDate = (value) => {
    if (!value) return "";
    return new Date(value).toLocaleString(undefined, {
      month: "short",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
    });
  };

  const slugify = (value = "") =>
    value
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "");

  const renderTags = (tags = []) => {
    const normalizedTags = tags
      .map((tag) => (typeof tag === "string" ? tag : tag?.name))
      .filter(Boolean);

    if (normalizedTags.length === 0) {
      return null;
    }

    return (
      <div className="summary-tags">
        {normalizedTags.map((tag) => (
          <span key={tag} className="tag-pill">
            {tag}
          </span>
        ))}
      </div>
    );
  };

  return (
    <div className="so-shell">
      <Sidebar active="questions" />

      <main className="so-main">
        <section className="feed-header">
          <div>
            <h1>{isSearchMode ? "Search Results" : "All Questions"}</h1>
            <p>
              {questions.length} {questions.length === 1 ? "result" : "results"}
              {isSearchMode && <> for &quot;{searchQuery}&quot;</>}
            </p>
          </div>
          {user && (
            <Link to="/ask" className="ask-button">
              Ask Question
            </Link>
          )}
        </section>

        <div className="feed-toolbar">
          <div className="feed-tabs" role="tablist" aria-label={isSearchMode ? "Search sorting" : "Feed sorting"}>
            {activeTabs.map((tab) => (
              <button
                key={tab.key}
                className={effectiveSort === tab.key ? "active" : ""}
                onClick={() => setSort(tab.key)}
                type="button"
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="feed-empty">{isSearchMode ? "Searching..." : "Loading questions..."}</div>
        ) : questions.length === 0 ? (
          <div className="feed-empty">
            {isSearchMode ? "No questions matched your search." : "No questions yet."}
          </div>
        ) : (
          <div className="question-list">
            {questions.map((q) => {
              const questionId = q.questionId || q.id;
              const activityDate = effectiveSort === "newest" ? q.createdAt : q.latestActivityAt;

              return (
                <article key={q.id || questionId} className="question-summary">
                  <div className="question-stats">
                    <div className="stat">
                      <strong>{q.voteCount ?? 0}</strong>
                      <span>votes</span>
                    </div>
                    <div className="stat answered">
                      <strong>{q.answerCount ?? 0}</strong>
                      <span>answers</span>
                    </div>
                  </div>

                  <div className="question-content">
                    <h2>
                      <Link to={`/questions/${questionId}/${slugify(q.title)}`}>{q.title}</Link>
                    </h2>
                    {q.body && <p className="question-excerpt">{q.body}</p>}

                    <div className="summary-footer">
                      {renderTags(q.tags)}
                      <div className="question-meta">
                        <span>
                          {effectiveSort === "newest" ? "asked" : "modified"} {formatDate(activityDate)}
                        </span>
                      </div>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}

      </main>
    </div>
  );
}

export default QuestionsPage;
