import { Link } from "react-router-dom";

function Sidebar({ active }) {
  return (
    <aside className="so-sidebar">
      <Link to="/questions" className={`so-sidebar-link ${active === "questions" ? "active" : ""}`}>
        Questions
      </Link>
      <Link to="/tags" className={`so-sidebar-link ${active === "tags" ? "active" : ""}`}>
        Tags
      </Link>
      <Link to="/users" className={`so-sidebar-link ${active === "users" ? "active" : ""}`}>
        Users
      </Link>
    </aside>
  );
}

export default Sidebar;
