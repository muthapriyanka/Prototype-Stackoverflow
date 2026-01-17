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

export const postQuestion = async (question) => {
  const response = await api.post("/questions", question);
  return response.data;
};

export const getTags = async () => {
  const res = await fetch("http://localhost:8080/tags");
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
