// Vercel Serverless Function: Secure Proxy for Quran Foundation Content API v4
// Supports OAuth2 Client Credentials (QF_CLIENT_ID / QF_CLIENT_SECRET in Vercel Env Vars)
// with graceful fallback to public api.quran.com/api/v4 when credentials are not yet configured.

const PUBLIC_API_BASE = "https://api.quran.com/api/v4";
const OAUTH_TOKEN_URL =
  process.env.QF_OAUTH_TOKEN_URL || "https://oauth2.quran.foundation/oauth2/token";
const OAUTH_API_BASE =
  process.env.QF_API_BASE_URL || "https://apis.quran.foundation/content/api/v4";

// In-memory token cache (per warm serverless instance)
let cachedToken = null;
let tokenExpiresAtMs = 0;

// Lightweight per-IP rate limiter (60 req/min on warm instances)
const rateBuckets = new Map();
const RATE_LIMIT_MAX = 60;
const RATE_WINDOW_MS = 60_000;

function checkRateLimit(ip) {
  const now = Date.now();
  const bucket = rateBuckets.get(ip);
  if (!bucket || now - bucket.windowStart > RATE_WINDOW_MS) {
    if (rateBuckets.size > 500) rateBuckets.clear();
    rateBuckets.set(ip, { windowStart: now, count: 1 });
    return true;
  }
  bucket.count += 1;
  return bucket.count <= RATE_LIMIT_MAX;
}

function sendJson(res, statusCode, payload, cacheSeconds = 0) {
  res.statusCode = statusCode;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
  if (cacheSeconds > 0 && statusCode === 200) {
    res.setHeader(
      "Cache-Control",
      `public, s-maxage=${cacheSeconds}, stale-while-revalidate=${cacheSeconds * 4}`
    );
  } else {
    res.setHeader("Cache-Control", "no-store");
  }
  res.end(JSON.stringify(payload));
}

async function getOAuthAccessToken() {
  const clientId = process.env.QF_CLIENT_ID;
  const clientSecret = process.env.QF_CLIENT_SECRET;
  if (!clientId || !clientSecret) return null;

  const now = Date.now();
  if (cachedToken && now < tokenExpiresAtMs - 60_000) {
    return { token: cachedToken, clientId };
  }

  const basicAuth = Buffer.from(`${clientId}:${clientSecret}`).toString("base64");
  const body = new URLSearchParams({
    grant_type: "client_credentials",
    scope: process.env.QF_OAUTH_SCOPE || "content"
  });

  const resp = await fetchWithRetry(OAUTH_TOKEN_URL, {
    method: "POST",
    headers: {
      Authorization: `Basic ${basicAuth}`,
      "Content-Type": "application/x-www-form-urlencoded",
      Accept: "application/json"
    },
    body: body.toString()
  });

  if (!resp.ok) {
    throw new Error(`OAuth2 token request failed with HTTP ${resp.status}`);
  }

  const data = await resp.json();
  if (!data.access_token) {
    throw new Error("OAuth2 response missing access_token");
  }
  const expiresInSec = Number(data.expires_in || 3600);
  cachedToken = data.access_token;
  tokenExpiresAtMs = now + expiresInSec * 1000;
  return { token: cachedToken, clientId };
}

async function fetchWithRetry(url, options = {}, maxRetries = 2) {
  let attempt = 0;
  let lastError = null;

  while (attempt <= maxRetries) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 8000);
    try {
      const response = await fetch(url, { ...options, signal: controller.signal });
      clearTimeout(timer);
      if (response.ok || ![429, 500, 502, 503, 504].includes(response.status)) {
        return response;
      }
    } catch (err) {
      clearTimeout(timer);
      lastError = err;
    }
    attempt += 1;
    if (attempt <= maxRetries) {
      await new Promise((r) => setTimeout(r, 250 * attempt));
    }
  }
  throw lastError || new Error("Upstream request failed after retries");
}

async function proxyQuranUpstream(pathWithQuery) {
  let auth = null;
  try {
    auth = await getOAuthAccessToken();
  } catch (err) {
    console.warn("QF OAuth2 unavailable, falling back to public Quran.com API v4");
  }

  const base = auth ? OAUTH_API_BASE : PUBLIC_API_BASE;
  const headers = { Accept: "application/json" };
  if (auth) {
    headers["x-auth-token"] = auth.token;
    headers["x-client-id"] = auth.clientId;
  }

  const upstreamUrl = `${base}${pathWithQuery}`;
  let response = await fetchWithRetry(upstreamUrl, { method: "GET", headers });

  // If OAuth token was rejected, refresh once or fall back to public API v4
  if (auth && (response.status === 401 || response.status === 403)) {
    cachedToken = null;
    tokenExpiresAtMs = 0;
    response = await fetchWithRetry(`${PUBLIC_API_BASE}${pathWithQuery}`, {
      method: "GET",
      headers: { Accept: "application/json" }
    });
  }

  if (!response.ok) {
    throw new Error(`Upstream API returned HTTP ${response.status}`);
  }
  return response.json();
}

function parseIntInRange(raw, min, max) {
  if (raw === null || raw === undefined || raw === "") return null;
  if (!/^\d+$/.test(String(raw))) return null;
  const n = Number(raw);
  return n >= min && n <= max ? n : null;
}

export default async function handler(req, res) {
  if (req.method === "OPTIONS") {
    res.statusCode = 204;
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type");
    res.end();
    return;
  }

  if (req.method !== "GET") {
    sendJson(res, 405, { error: "Method not allowed" });
    return;
  }

  const forwarded = req.headers["x-forwarded-for"];
  const clientIp = (Array.isArray(forwarded) ? forwarded[0] : forwarded || "unknown")
    .split(",")[0]
    .trim();

  if (!checkRateLimit(clientIp)) {
    sendJson(res, 429, { error: "Rate limit exceeded. Please wait a moment." });
    return;
  }

  const url = new URL(req.url || "/api/quran", "https://localhost");
  const routeParam = url.searchParams.get("route") || "";
  const segments = routeParam
    .split("/")
    .map((s) => s.trim())
    .filter(Boolean);

  const lang = url.searchParams.get("language") === "en" ? "en" : "ar";

  try {
    // GET /api/quran/chapters
    if (segments.length === 1 && segments[0] === "chapters") {
      const data = await proxyQuranUpstream(`/chapters?language=${lang}`);
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/chapters/:id
    if (segments.length === 2 && segments[0] === "chapters") {
      const id = parseIntInRange(segments[1], 1, 114);
      if (!id) {
        sendJson(res, 400, { error: "Chapter id must be between 1 and 114" });
        return;
      }
      const data = await proxyQuranUpstream(`/chapters/${id}?language=${lang}`);
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/pages/:number
    if (segments.length === 2 && segments[0] === "pages") {
      const pageNum = parseIntInRange(segments[1], 1, 604);
      if (!pageNum) {
        sendJson(res, 400, { error: "Mushaf page number must be between 1 and 604" });
        return;
      }
      const translations = url.searchParams.get("translations");
      const trQuery =
        translations && /^\d+(,\d+)*$/.test(translations)
          ? `&translations=${translations}`
          : "";
      const fields =
        "text_uthmani,verse_key,verse_number,chapter_id,page_number,juz_number,hizb_number";
      const data = await proxyQuranUpstream(
        `/verses/by_page/${pageNum}?words=false&fields=${fields}&per_page=50${trQuery}`
      );
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/verses?chapter=1&page=1
    if (segments.length === 1 && segments[0] === "verses") {
      const chapterId = parseIntInRange(url.searchParams.get("chapter"), 1, 114);
      const pageParam = parseIntInRange(url.searchParams.get("page"), 1, 20) || 1;
      const verseKey = url.searchParams.get("verse_key");
      const fields =
        "text_uthmani,verse_key,verse_number,chapter_id,page_number,juz_number,hizb_number";

      if (verseKey && /^\d{1,3}:\d{1,3}$/.test(verseKey)) {
        const data = await proxyQuranUpstream(
          `/verses/by_key/${verseKey}?fields=${fields}`
        );
        sendJson(res, 200, data, 3600);
        return;
      }

      if (!chapterId) {
        sendJson(res, 400, { error: "Provide valid chapter (1..114) or verse_key" });
        return;
      }

      const data = await proxyQuranUpstream(
        `/verses/by_chapter/${chapterId}?words=false&fields=${fields}&per_page=50&page=${pageParam}`
      );
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/tafsirs (?tafsir_id=16&verse_key=1:1)
    if (segments.length === 1 && segments[0] === "tafsirs") {
      const tafsirId = parseIntInRange(url.searchParams.get("tafsir_id"), 1, 9999);
      const verseKey = url.searchParams.get("verse_key");
      if (tafsirId && verseKey && /^\d{1,3}:\d{1,3}$/.test(verseKey)) {
        const data = await proxyQuranUpstream(
          `/verses/by_key/${verseKey}?fields=text_uthmani&tafsirs=${tafsirId}`
        );
        sendJson(res, 200, data, 3600);
        return;
      }
      const data = await proxyQuranUpstream(`/resources/tafsirs?language=${lang}`);
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/translations
    if (segments.length === 1 && segments[0] === "translations") {
      const data = await proxyQuranUpstream(`/resources/translations?language=${lang}`);
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/recitations
    if (segments.length === 1 && segments[0] === "recitations") {
      const data = await proxyQuranUpstream(`/resources/recitations?language=${lang}`);
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/audio?recitation=7&chapter=1
    if (segments.length === 1 && segments[0] === "audio") {
      const recitationId = parseIntInRange(url.searchParams.get("recitation"), 1, 999) || 7;
      const chapterId = parseIntInRange(url.searchParams.get("chapter"), 1, 114);
      if (!chapterId) {
        sendJson(res, 400, { error: "Valid chapter (1..114) is required" });
        return;
      }
      const data = await proxyQuranUpstream(
        `/chapter_recitations/${recitationId}/${chapterId}`
      );
      sendJson(res, 200, data, 3600);
      return;
    }

    // GET /api/quran/search?q=...
    if (segments.length === 1 && segments[0] === "search") {
      const q = (url.searchParams.get("q") || "").trim();
      if (!q || q.length > 120) {
        sendJson(res, 400, { error: "Search query q must be 1..120 characters" });
        return;
      }
      const size = parseIntInRange(url.searchParams.get("size"), 1, 50) || 20;
      const page = parseIntInRange(url.searchParams.get("page"), 1, 50) || 1;
      const encoded = encodeURIComponent(q);
      const data = await proxyQuranUpstream(
        `/search?q=${encoded}&size=${size}&page=${page}&language=${lang}`
      );
      sendJson(res, 200, data, 600);
      return;
    }

    sendJson(res, 404, {
      error: "Unknown Quran API route",
      availableRoutes: [
        "/api/quran/chapters",
        "/api/quran/chapters/:id",
        "/api/quran/pages/:number",
        "/api/quran/verses?chapter=1",
        "/api/quran/tafsirs",
        "/api/quran/translations",
        "/api/quran/recitations",
        "/api/quran/audio?recitation=7&chapter=1",
        "/api/quran/search?q=..."
      ]
    });
  } catch (err) {
    console.error("Quran API proxy error:", err instanceof Error ? err.message : "unknown");
    sendJson(res, 502, {
      error: "Unable to fetch verified Quran content from upstream API"
    });
  }
}
