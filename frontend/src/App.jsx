import { useState } from 'react'
import './App.css'

const apiBase = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

function App() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [token, setToken] = useState('')
  const [me, setMe] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function register() {
    setError('')
    setBusy(true)
    try {
      const res = await fetch(`${apiBase}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setError(data.message ?? JSON.stringify(data))
        return
      }
      setToken(data.accessToken ?? '')
      setMe(null)
    } catch (e) {
      setError(String(e))
    } finally {
      setBusy(false)
    }
  }

  async function login() {
    setError('')
    setBusy(true)
    try {
      const res = await fetch(`${apiBase}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setError(data.message ?? JSON.stringify(data))
        return
      }
      setToken(data.accessToken ?? '')
      setMe(null)
    } catch (e) {
      setError(String(e))
    } finally {
      setBusy(false)
    }
  }

  async function fetchMe() {
    setError('')
    setBusy(true)
    try {
      const res = await fetch(`${apiBase}/api/users/me`, {
        headers: {
          Authorization: token ? `Bearer ${token}` : '',
          Accept: 'application/json',
        },
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setError(data.message ?? JSON.stringify(data))
        setMe(null)
        return
      }
      setMe(data)
    } catch (e) {
      setError(String(e))
      setMe(null)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="wrap">
      <h1>User service (via API gateway)</h1>
      <p className="hint">
        Gateway: <code>{apiBase}</code> — start Postgres, user-service (8081), then api-gateway (8080), then{' '}
        <code>npm run dev</code>.
      </p>

      <label>
        Email
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoComplete="email"
        />
      </label>
      <label>
        Password (min 8 chars)
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
        />
      </label>

      <div className="row">
        <button type="button" disabled={busy} onClick={register}>
          Register
        </button>
        <button type="button" disabled={busy} onClick={login}>
          Login
        </button>
        <button type="button" disabled={busy || !token} onClick={fetchMe}>
          GET /api/users/me
        </button>
      </div>

      {error ? <p className="err">{error}</p> : null}

      <section>
        <h2>JWT (accessToken)</h2>
        <pre className="box">{token || '—'}</pre>
      </section>

      <section>
        <h2>/me response</h2>
        <pre className="box">{me ? JSON.stringify(me, null, 2) : '—'}</pre>
      </section>
    </div>
  )
}

export default App
