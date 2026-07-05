import { useEffect, useState } from "react";
import Sidebar from "./Sidebar";
import { getUsers } from "./services/api";
import "./QuestionsPage.css";

function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getUsers()
      .then((data) => setUsers(Array.isArray(data) ? data : []))
      .catch((err) => {
        console.error("Failed to load users:", err);
        setUsers([]);
      })
      .finally(() => setLoading(false));
  }, []);

  const formatDate = (value) => {
    if (!value) return "";
    return new Date(value).toLocaleDateString(undefined, {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  return (
    <div className="so-shell">
      <Sidebar active="users" />

      <main className="so-main">
        <section className="feed-header">
          <div>
            <h1>Users</h1>
            <p>{users.length} users</p>
          </div>
        </section>

        {loading ? (
          <div className="feed-empty">Loading users...</div>
        ) : users.length === 0 ? (
          <div className="feed-empty">No users yet.</div>
        ) : (
          <div className="directory-grid">
            {users.map((user) => (
              <article key={user.id} className="user-card">
                <div className="user-avatar">{user.username?.slice(0, 1).toUpperCase()}</div>
                <div>
                  <h2>{user.username}</h2>
                  <p>{user.email}</p>
                  <span>joined {formatDate(user.createdAt)}</span>
                </div>
              </article>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

export default UsersPage;
