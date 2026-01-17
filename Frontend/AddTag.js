import React, { useState } from "react";
import { createTag } from "./services/api";

function AddTag() {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createTag({ name, description });
    setName("");
    setDescription("");
  };

  return (
    <div className="container mt-3">
      <h4>Add Tag</h4>

      <form onSubmit={handleSubmit}>
        <input
          className="form-control mb-2"
          placeholder="tag name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <textarea
          className="form-control mb-2"
          placeholder="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <button className="btn btn-primary">Save</button>
      </form>
    </div>
  );
}

export default AddTag;
