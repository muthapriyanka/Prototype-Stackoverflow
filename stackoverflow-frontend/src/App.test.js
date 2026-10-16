import { render, screen } from "@testing-library/react";

jest.mock("axios", () => ({
  create: () => ({ post: jest.fn() }),
  get: jest.fn(),
}));

const App = require("./App").default;

test("renders the login screen", () => {
  render(<App />);
  expect(screen.getByRole("heading", { name: "Login" })).toBeInTheDocument();
  expect(screen.getByPlaceholderText("Username")).toBeInTheDocument();
  expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
});
