import axios from "axios";

// Base URL for your backend
const API_BASE_URL = "http://localhost:8080";

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});


export const getQuestions = async () => {
  try {
    const response = await axios.get("http://localhost:8080/questions");
    return response.data;
  } catch (err) {
    console.error("API error:", err);
    return [];
  }
};


// services/api.js
export const getQuestionswithans = async () => {
  const res = await fetch("http://localhost:8080/questions/with-answers");
  return await res.json();
};

export const getFeed = async (sort = "active") => {
  const res = await fetch(`http://localhost:8080/feed?sort=${sort}`);
  if (!res.ok) {
    throw new Error("Failed to load feed");
  }
  const feed = await res.json();

  if (!Array.isArray(feed)) {
    return [];
  }

  const hasMissingDetails = feed.some(
    (item) => !item.body || !Array.isArray(item.tags) || item.tags.length === 0
  );

  if (!hasMissingDetails) {
    return feed;
  }

  try {
    const questions = await getQuestions();
    const questionById = new Map(questions.map((question) => [question.id, question]));

    return feed.map((item) => {
      const question = questionById.get(item.questionId || item.id);

      if (!question) {
        return item;
      }

      return {
        ...item,
        body: item.body || question.body,
        tags: Array.isArray(item.tags) && item.tags.length > 0 ? item.tags : question.tags,
      };
    });
  } catch (err) {
    console.error("Failed to enrich feed questions:", err);
    return feed;
  }
};

export const searchQuestions = async (query, sort = "relevance") => {
  const params = new URLSearchParams({ q: query, sort });
  const res = await fetch(`http://localhost:8080/search?${params.toString()}`);
  if (!res.ok) {
    throw new Error("Failed to search questions");
  }
  return await res.json();
};

export const postQuestion = async (question) => {
  const response = await api.post("/questions", question);
  return response.data;
};

export const getTags = async () => {
  const res = await fetch("http://localhost:8080/tags");
  return res.json();
};

export const getUsers = async () => {
  const res = await fetch("http://localhost:8080/users");
  if (!res.ok) {
    throw new Error("Failed to load users");
  }
  return res.json();
};

export const createTag = async (tag) => {
  const res = await fetch("http://localhost:8080/tags", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(tag),
  });
  return res.json();
};
