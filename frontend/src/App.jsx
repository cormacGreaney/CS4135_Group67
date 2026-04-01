import { useCallback, useEffect, useState } from 'react'
import './App.css'

// All API calls go through the gateway (8080), not straight to each microservice.
const apiBase = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

function App() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [token, setToken] = useState('')
  const [me, setMe] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  // Product list from the catalog API (no login needed to view)
  const [products, setProducts] = useState(null)
  const [productQuery, setProductQuery] = useState('')
  const [productsError, setProductsError] = useState('')

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

  const loadProducts = useCallback(async (q) => {
    // Pass a search string to filter products by name (server does the filtering)
    setProductsError('')
    setBusy(true)
    try {
      const qs = q.trim() ? `?q=${encodeURIComponent(q.trim())}` : ''
      const res = await fetch(`${apiBase}/api/products${qs}`, { headers: { Accept: 'application/json' } })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setProductsError(data.message ?? JSON.stringify(data))
        setProducts(null)
        return
      }
      setProducts(data)
    } catch (e) {
      setProductsError(String(e))
      setProducts(null)
    } finally {
      setBusy(false)
    }
  }, [apiBase])

  useEffect(() => {
    void loadProducts('')
  }, [loadProducts])

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
        Gateway: <code>{apiBase}</code> — start Postgres (user + product DBs), user-service (8081), product-service
        (8082), api-gateway (8080), then <code>npm run dev</code>.
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

      <section>
        <h2>Catalog (GET /api/products)</h2>
        <div className="row">
          <label className="inline">
            Search (q)
            <input
              type="search"
              value={productQuery}
              onChange={(e) => setProductQuery(e.target.value)}
              placeholder="name contains…"
            />
          </label>
          <button type="button" disabled={busy} onClick={() => void loadProducts(productQuery)}>
            Search / refresh
          </button>
        </div>
        {productsError ? <p className="err">{productsError}</p> : null}
        <pre className="box">{products ? JSON.stringify(products, null, 2) : 'Loading…'}</pre>
      </section>
    </div>
  )
}

export default App
