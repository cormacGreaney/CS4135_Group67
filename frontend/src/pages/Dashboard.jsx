import { useEffect, useState } from "react";
import { apiFetch } from "../api/api.js";

function Dashboard() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    apiFetch("/api/users/me")
      .then(setUser)
      .catch(err => alert(err.message));
  }, []);

  if (!user) return <p>Loading...</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>User Dashboard</h2>
      <p>Email: {user.email}</p>
      <p>Role: {user.role}</p>
    </div>
  );
}

export default Dashboard;